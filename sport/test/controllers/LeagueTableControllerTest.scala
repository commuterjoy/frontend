package test

import play.api.test._
import play.api.test.Helpers._
import org.scalatest.{DoNotDiscover, Matchers, FlatSpec}

@DoNotDiscover class LeagueTableControllerTest extends FlatSpec with Matchers with ConfiguredTestSuite {
  
  "League Table Controller" should "200 when content type is table" in {
    val result = football.controllers.LeagueTableController.renderLeagueTable()(TestRequest())
    status(result) should be(200)
  }

  it should "return JSONP when callback is supplied to table" in {
    val fakeRequest = FakeRequest(GET, "/football/tables?callback=foo")
      .withHeaders("host" -> "localhost:9000")
      .withHeaders("Accept" -> "application/javascript")
    val result = football.controllers.LeagueTableController.renderLeagueTable()(fakeRequest)
    status(result) should be(200)
    header("Content-Type", result).get should be("application/javascript; charset=utf-8")
  }

  it should "return JSON when .json format is supplied to table" in {
    val fakeRequest = FakeRequest(GET, "/football/tables.json")
      .withHeaders("host" -> "localhost:9000")
      .withHeaders("Origin" -> "http://www.theorigin.com")
      
    val result = football.controllers.LeagueTableController.renderLeagueTableJson()(fakeRequest)
    status(result) should be(200)
    header("Content-Type", result).get should be("application/json; charset=utf-8")
    contentAsString(result) should startWith("{\"config\"")
  }
  
  it should "200 when content type is teams" in {
    val result = football.controllers.LeagueTableController.renderTeamlist()(TestRequest().withHeaders("Accept" -> "application/javascript"))
    status(result) should be(200)
  }

  it should "return JSONP when callback is supplied to teams" in {
    val fakeRequest = FakeRequest(GET, "/football/teams?callback=foo")
      .withHeaders("host" -> "localhost:9000")
      .withHeaders("Accept" -> "application/javascript")
    val result = football.controllers.LeagueTableController.renderTeamlist()(fakeRequest)
    status(result) should be(200)
    header("Content-Type", result).get should be("application/javascript; charset=utf-8")
  }

  it should "return JSON when .json format is supplied to teams" in {
    val fakeRequest = FakeRequest(GET, "/football/teams.json")
      .withHeaders("host" -> "localhost:9000")
      .withHeaders("Origin" -> "http://www.theorigin.com")
      
    val result = football.controllers.LeagueTableController.renderTeamlist()(fakeRequest)
    status(result) should be(200)
    header("Content-Type", result).get should be("application/json; charset=utf-8")
    contentAsString(result) should startWith("{\"config\"")
  }
  
  val competitionId = "premierleague"
  
  it should "200 when content type is competition table" in {
    val result = football.controllers.LeagueTableController.renderCompetition(competitionId)(TestRequest())
    status(result) should be(200)
  }

  it should "return JSONP when callback is supplied to competition table" in {
    val fakeRequest = FakeRequest(GET, "/football/" + competitionId + "/table?callback=foo")
      .withHeaders("host" -> "localhost:9000")
      .withHeaders("Accept" -> "application/javascript")
    val result = football.controllers.LeagueTableController.renderCompetition(competitionId)(fakeRequest)
    status(result) should be(200)
    header("Content-Type", result).get should be("application/javascript; charset=utf-8")
  }

  it should "return JSON when .json format is supplied to competition table" in {
    val fakeRequest = FakeRequest(GET, "/football/" + competitionId + "/table.json")
      .withHeaders("host" -> "localhost:9000")
      .withHeaders("Origin" -> "http://www.theorigin.com")
      
    val result = football.controllers.LeagueTableController.renderCompetition(competitionId)(fakeRequest)
    status(result) should be(200)
    header("Content-Type", result).get should be("application/json; charset=utf-8")
    contentAsString(result) should startWith("{\"html\"")
  }
  
}