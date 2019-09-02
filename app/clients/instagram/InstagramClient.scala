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

  def getProfile(username: String): Future[Option[InstagramProfile]] = {
    val profileUrl = getUsernameRequestUrl(username)
    sendRequest(profileUrl).map(response => {
      response.flatMap(res => Json.parse(res.body).asOpt[InstagramProfile]
      )
    })
  }

  private def sendRequest(url: String): Future[Option[Response]] = {
    Future {
      try {
        val result = Http(url)
          .header("Accept", "application/json")
          .option(HttpOptions.readTimeout(4000)).asString
        if (result.code == 200) {
          Some(Response(result.code.toString, result.body))
        } else {
          log.error(s"Error is getting profile from Instagram url: ${url}")
          None
        }
      } catch {
        case e: Exception => {
          log.error(s"Error is getting profile from Instagram url: ${url} with Exception: ${e.getMessage}")
          None
        }
      }
    }
  }

  private def getUsernameRequestUrl(username: String): String = s"${baseUrl}${username}/?__a=1"

}
