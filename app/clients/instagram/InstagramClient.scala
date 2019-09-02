package clients.instagram

import clients.Response
import javax.inject.Inject
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.libs.json.Json
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class InstagramClient {

  val baseUrl = "https://www.instagram.com/"

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  def getProfile(username: String): Future[Either[models.Error, InstagramProfile]] = {
    val profileUrl = getUsernameRequestUrl(username)
    sendRequest(profileUrl).map(_.fold(
      error => Left(error),
      res => {
        val profileOpt = Json.parse(res.body).asOpt[InstagramProfile]
        if (profileOpt.isDefined) {
          Right(profileOpt.get)
        } else {
          Left(models.Error("INSTAGRAM_API_ERROR", "Error to deserialize Profile"))
        }
      }
    ))
  }

  private def sendRequest(url: String): Future[Either[models.Error, Response]] = {
    Future {
      try {
        val result = Http(url)
          .header("Accept", "application/json")
          .option(HttpOptions.followRedirects(true))
          .option(HttpOptions.readTimeout(4000)).asString
        if (result.code == 200) {
          Right(Response(result.code.toString, result.body))
        } else {
          val msg = s"Error in getting profile from Instagram url: ${url} with response code: ${result.code}"
          log.error(msg)
          Left(models.Error("INSTAGRAM_API_ERROR", msg))
        }
      } catch {
        case e: Exception => {
          val msg = s"Error in getting profile from Instagram url: ${url} with Exception: ${e.getMessage}"
          Left(models.Error("INSTAGRAM_API_ERROR", msg))
        }
      }
    }
  }

  private def getUsernameRequestUrl(username: String): String =
    s"${baseUrl}${username}/?__a=1"

}
