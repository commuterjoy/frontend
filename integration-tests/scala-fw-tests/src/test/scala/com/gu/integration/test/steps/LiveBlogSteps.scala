package com.gu.integration.test.steps

import org.openqa.selenium.WebDriver
import org.scalatest.Matchers

import com.gu.automation.support.TestLogging
import com.gu.integration.test.util.PageLoader._
import com.gu.integration.test.util.ElementLoader._
import com.gu.integration.test.pages.article.LiveBlogPage

case class LiveBlogSteps(implicit driver: WebDriver) extends TestLogging with Matchers {

  def goToLiveBlog(relativeLiveArticleUrl: String): LiveBlogPage = {
    logger.step(s"I am an Live Article page with relative url: $relativeLiveArticleUrl")
    lazy val liveArticle = new LiveBlogPage()
    goTo(fromRelativeUrl(relativeLiveArticleUrl), liveArticle)
  }

  def openExpandSection(liveBlogPage: LiveBlogPage) = {
    logger.step("Clicking the expand button")
    liveBlogPage.liveBlogBlocks.findVisibleDirectElements("div") should not be empty
    liveBlogPage.liveBlogBlocks.findHiddenDirectElements("div") should not be empty
    liveBlogPage.expandButton.click()
  }

  def checkExpandedSectionContent(liveBlogPage: LiveBlogPage) = {
    logger.step("Checking expanded content")
	liveBlogPage.expandButtonNotPresent should be (true)
    liveBlogPage.liveBlogBlocks.findHiddenDirectElements("div") should be (empty)
  }
}