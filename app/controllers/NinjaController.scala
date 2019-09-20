package controllers

import clients.managers.NinjaManager
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NinjaController @Inject()(implicit ws: WSClient) extends Controller {

  val ninjaManager = new NinjaManager()

  def home() = Action.async(parse.anyContent) { request =>
    Future.successful(Ok("Let's get this BREAD!"))
  }

  def getProfiles(withContent: Option[Boolean], withSelectedContent: Option[Boolean]) = Action.async(parse.anyContent) { request =>
    ninjaManager.getProfiles(withContent, withSelectedContent).map(_.fold(
      error => InternalServerError(Json.toJson(error)),
      res => Ok(Json.toJson(res))
    ))

  }

  def getProfile(username: String) = Action.async(parse.anyContent) { request =>
    ninjaManager.getProfile(username).map(_.fold(
      error => InternalServerError(Json.toJson(error)),
      res => Ok(Json.toJson(res))
    ))
  }

  def getNinja(ninjaId: String) = Action.async(parse.anyContent) { request =>
    ninjaManager.getNinja(ninjaId).map(_.fold(
      error => InternalServerError(Json.toJson(error)),
      res => Ok(Json.toJson(res))
    ))
  }

  def addNinja(ninjaId: String) = Action.async(parse.json) { request =>
    val maybeLastAlarm = (request.body \ "lastAlarm").asOpt[String]
    val maybeLastUsername = (request.body \ "lastUsername").asOpt[String]
    ninjaManager.addNinja(ninjaId, maybeLastAlarm, maybeLastUsername).map(_.fold(
      error => InternalServerError(Json.toJson(error)),
      res => Ok(Json.toJson(res))
    ))
  }

  def updateNinja(ninjaId: String) = Action.async(parse.json) { request =>
    val maybeLastAlarm = (request.body \ "lastAlarm").asOpt[String]
    val maybeLastUsername = (request.body \ "lastUsername").asOpt[String]
    ninjaManager.updateNinja(ninjaId, maybeLastAlarm, maybeLastUsername).map(_.fold(
      error => InternalServerError(Json.toJson(error)),
      res => Ok(Json.toJson(res))
    ))
  }

  def getAccounts() = Action.async(parse.anyContent) { request =>
    ninjaManager.getAccounts.map(_.fold(
      error => InternalServerError(Json.toJson(error)),
      res => Ok(Json.toJson(res))
    ))
  }

  def updateAccount(username: String, thumbsUp: Option[Boolean], gym: Option[Boolean], sleepy: Option[Boolean]) = Action.async(parse.anyContent) { request =>
    ninjaManager.updateAccount(username, thumbsUp.getOrElse(false), gym.getOrElse(false), sleepy.getOrElse(false)).map(_.fold(
      error => InternalServerError(Json.toJson(error)),
      res => Ok(Json.toJson(res))
    ))
  }


}
