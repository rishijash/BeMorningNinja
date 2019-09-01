package clients.instagram

import clients.Response
import javax.inject.Inject
import play.api.Configuration
import play.api.libs.json.Json
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class InstagramClient {

  val baseUrl = "https://www.instagram.com/"

  def getProfile(username: String): Future[Option[InstagramProfile]] = {
    val profileUrl = getUsernameRequestUrl(username)
    sendRequest(profileUrl).map(response => {
      response.flatMap(res => Json.parse(res.body).asOpt[InstagramProfile]
      )
    })
  }

  private def sendRequest(url: String): Future[Option[Response]] = {
    Future {
      val result = Http(url)
        .header("Accept", "application/json")
        .option(HttpOptions.readTimeout(4000)).asString
      if (result.code == 200) {
        Some(Response(result.code.toString, result.body))
      } else {
        None
      }
    }
  }

  private def getUsernameRequestUrl(username: String): String = s"${baseUrl}${username}/?__a=1"

}
