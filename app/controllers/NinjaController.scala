package controllers

import clients.managers.NinjaManager
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NinjaController extends Controller {

  val ninjaManager = new NinjaManager()

  def home() = Action.async(parse.anyContent) { request =>
    Future.successful(Ok("Let's get this BREAD!"))
  }

  def getProfiles() = Action.async(parse.anyContent) { request =>
    ninjaManager.getProfiles().map(res =>
      Ok(Json.toJson(res))
    )
  }

  def getProfile(username: String) = Action.async(parse.anyContent) { request =>
    ninjaManager.getProfile(username).map(res =>
      Ok(Json.toJson(res))
    )
  }


}
