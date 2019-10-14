package clients.instagram

import clients.Response
import javax.inject.Inject
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import scalaj.http.{Http, HttpOptions}
import util.{HtmlUtil, ReqBinUtil, TimeoutUtil, UserAgentUtil}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import scala.util.control.NonFatal

class InstagramClient @Inject() (implicit ws: WSClient) {

  val baseUrl = "https://www.instagram.com/"

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  def getProfile(username: String): Future[Either[models.Error, InstagramProfile]] = {
    val profileUrl = getUsernameRequestUrl(username)
    ReqBinUtil.sendRequestNoReqBin(profileUrl).map(_.fold(
      error => Left(error),
      res => {
        try {
          val profileOpt = Json.parse(res.body).validateOpt[InstagramProfile].asOpt.flatten
          if (profileOpt.isDefined) {
            Right(profileOpt.get)
          } else {
            Left(models.Error("INSTAGRAM_API_ERROR", "Error to deserialize Profile"))
          }
        } catch {
          case e: Exception => {
            Left(models.Error("INSTAGRAM_API_ERROR", "Error to deserialize Profile"))
          }
        }
      }
    ))
  }

  def getInstagramProfileWeb(username: String): Future[Either[models.Error, InstagramProfile]] = {
    val profileUrl = getUsernameRequestUrlWeb(username)
    try {
      HtmlUtil.getHtmlFromUrl(profileUrl).map(htmlRes => {
        if (htmlRes.isDefined) {
          val html = htmlRes.get
          val startKeyIndex = html.indexOf("{\"logging_page_id\"")
          val endKeyIndex = html.indexOf("]},\"hostname\":\"")
          val subStringHtml = html.substring(startKeyIndex, endKeyIndex)
          val profileOpt = Json.parse(subStringHtml).validateOpt[InstagramProfile].asOpt.flatten
          if (profileOpt.isDefined) {
            Right(profileOpt.get)
          } else {
            Left(models.Error("INSTAGRAM_API_ERROR", "Error to deserialize Profile"))
          }
        } else {
          val msg = s"Error in getting profile from Instagram url: ${profileUrl}"
          Left(models.Error("INSTAGRAM_API_ERROR", msg))
        }
      })
    } catch {
      case e: Exception => {
        val msg = s"Error in getting profile from Instagram url: ${profileUrl} with Exception: ${e.getMessage}"
        Future.successful(Left(models.Error("INSTAGRAM_API_ERROR", msg)))
      }
    }
  }

  def getVideoLinkWebSync(postUrl: String): Option[String] = {
    val future = getVideoLinkWeb(postUrl)
    val timeoutSec = TimeoutUtil.getRandomTimeoutInSec
    Await.result(future, timeoutSec.seconds)
  }

  def getVideoLinkWeb(postUrl: String): Future[Option[String]] = {
    try {
      HtmlUtil.getHtmlFromUrl(postUrl).map(_.map(html => {
//        val startKeyIndex = html.indexOf("https://scontent-iad3-1.cdninstagram.com/v/")
        val startKeyIndex = html.indexOf("og:video\" content=\"https://scontent") + 19
        val subStringHtml = html.substring(startKeyIndex, html.length)
        val endKeyIndex = subStringHtml.indexOf("\"")
        val url = subStringHtml.substring(0, endKeyIndex).replaceAll("\\\\u0026", "&")
        url
      }))
    } catch {
      case NonFatal(e) => {
        Future.successful(None)
      }
    }
  }

  private def getUsernameRequestUrl(username: String): String =
    s"${baseUrl}${username}/?__a=1"

  private def getUsernameRequestUrlWeb(username: String): String =
    s"${baseUrl}${username}/"
}
