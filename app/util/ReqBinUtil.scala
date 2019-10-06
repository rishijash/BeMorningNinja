package util

import clients.Response
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

object ReqBinUtil {

  private val reqbinUrl = "https://reqbin.com/api/v1/Requests"
  private val log = LoggerFactory.getLogger(this.getClass.getName)

  def sendRequest(url: String): Future[Either[models.Error, Response]] = {
    Future {
      try {
        // Convert Request to ReqBin Request
        val authJson = Json.obj("auth" -> "noAuth", "bearerToken" -> "",
          "basicUsername" -> "", "basicPassword" -> "", "customHeader" -> "")
        val requestJosn = Json.obj("method" -> "GET",
          "url" -> url,
          "contentType" -> "JSON",
          "content" -> "", "headers" -> "", "auth" -> authJson)
        val request = Json.obj("id" -> "0", "name" -> "", "json" -> Json.stringify(requestJosn))
        val requestStr = Json.stringify(request)
        val userAgent = Random.shuffle(UserAgentUtil.userAgents).head
        val result = Http(reqbinUrl)
          .header("Accept", "application/json")
          .header("Content-Type", "application/json")
          .header("User-Agent", userAgent)
          .postData(requestStr)
          .asString
        if (result.is2xx) {
          // Convert Rbin Response to Response
          val jsonRes = Json.parse(result.body)
          val jsonContent = (jsonRes \ "Content").asOpt[String]
          Right(Response(result.code.toString, jsonContent.getOrElse("")))
        } else {
          val msg = s"Error in getting profile from Instagram url: ${url} with response code: ${result.code}."
          log.error(msg)
          log.error("Result: " + result.toString)
          Left(models.Error("API_ERROR", msg))
        }
      } catch {
        case e: Exception => {
          val msg = s"Error in getting profile from Instagram url: ${url} with Exception: ${e.getMessage}"
          Left(models.Error("API_ERROR", msg))
        }
      }
    }
  }

}
