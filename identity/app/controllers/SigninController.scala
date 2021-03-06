package controllers

import implicits.Forms
import play.api.mvc._
import play.api.data._
import play.api.data.validation.Constraints
import model.{NoCache, IdentityPage}
import common.ExecutionContexts
import services.{PlaySigninService, IdentityUrlBuilder, IdRequestParser, ReturnUrlVerifier}
import com.google.inject.{Inject, Singleton}
import idapiclient.IdApiClient
import play.api.i18n.Messages
import idapiclient.EmailPassword
import utils.SafeLogging
import form.Mappings
import scala.concurrent.Future


@Singleton
class SigninController @Inject()(returnUrlVerifier: ReturnUrlVerifier,
                                 api: IdApiClient,
                                 idRequestParser: IdRequestParser,
                                 idUrlBuilder: IdentityUrlBuilder,
                                 signInService : PlaySigninService)
  extends Controller with ExecutionContexts with SafeLogging with Mappings with Forms {

  val page = IdentityPage("/signin", "Sign in", "signin")

  val form = Form(
    Forms.tuple(
      "email" -> idEmail
        .verifying(Constraints.nonEmpty),
      "password" -> Forms.text
        .verifying(Constraints.nonEmpty),
      "keepMeSignedIn" -> Forms.boolean
    )
  )

  def renderForm(returnUrl: Option[String]) = Action { implicit request =>

    val filledForm = form.bindFromFlash.getOrElse(form.fill("", "", true))

    logger.trace("Rendering signin form")
    val idRequest = idRequestParser(request)
    NoCache(Ok(views.html.signin(page, idRequest, idUrlBuilder, filledForm)))
  }

  def processForm = Action.async { implicit request =>
    val idRequest = idRequestParser(request)
    val boundForm = form.bindFromRequest
    val verifiedReturnUrlAsOpt = returnUrlVerifier.getVerifiedReturnUrl(request)

    def onError(formWithErrors: Form[(String, String, Boolean)]): Future[Result] = {
      logger.info("Invalid login form submission")
      Future.successful {
        redirectToSigninPage(formWithErrors, verifiedReturnUrlAsOpt)
      }
    }

    def onSuccess(form: (String, String, Boolean)): Future[Result] = form match {
      case (email, password, rememberMe) =>
        logger.trace("authing with ID API")
        val authResponse = api.authBrowser(EmailPassword(email, password, idRequest.clientIp), idRequest.trackingData, Some(rememberMe))
        signInService.getCookies(authResponse, rememberMe) map {
          case Left(errors) =>
            logger.error(errors.toString())
            logger.info(s"Auth failed for user, ${errors.toString()}")
            val formWithErrors = errors.foldLeft(boundForm) { (formFold, error) =>
              val errorMessage =
                if ("Invalid email or password" == error.message) Messages("error.login")
                else error.description
              formFold.withError(error.context.getOrElse(""), errorMessage)
            }

            redirectToSigninPage(formWithErrors, verifiedReturnUrlAsOpt)

          case Right(responseCookies) =>
            logger.trace("Logging user in")
            SeeOther(verifiedReturnUrlAsOpt.getOrElse(returnUrlVerifier.defaultReturnUrl))
              .withCookies(responseCookies:_*)
        }
    }

    boundForm.fold[Future[Result]](onError, onSuccess)
  }

  def redirectToSigninPage(formWithErrors: Form[(String, String, Boolean)], returnUrl: Option[String]): Result = {
    NoCache(SeeOther(routes.SigninController.renderForm(returnUrl).url).flashing(clearPassword(formWithErrors).toFlash))
  }

  private def clearPassword(formWithPassword: Form[(String, String, Boolean)]) = {
    val dataWithoutPassword = formWithPassword.data + ("password" -> "")
    formWithPassword.copy(data = dataWithoutPassword)
  }
}
