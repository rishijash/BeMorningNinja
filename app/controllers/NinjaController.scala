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

  def getProfiles(withContent: Option[Boolean]) = Action.async(parse.anyContent) { request =>
    ninjaManager.getProfiles(withContent).map(_.fold(
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

  def responseChecker() = Action.async(parse.json) { request =>
    val url = (request.body \ "url").asOpt[String]
    if (url.isDefined) {
      ninjaManager.responseChecker(url.get).map(_.fold(
        error => InternalServerError(Json.toJson(error)),
        res => Ok(Json.toJson(res))
      ))
    } else {
      Future.successful(InternalServerError)
    }
  }


}
