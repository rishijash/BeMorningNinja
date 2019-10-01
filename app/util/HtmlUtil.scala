package util

import org.slf4j.LoggerFactory
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object HtmlUtil {

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  def getHtmlFromUrl(url: String): Future[Option[String]] = {
    sendRequest(url).map(htmlCode => {
      htmlCode
    })
  }

  private def sendRequest(url: String): Future[Option[String]] = {
    Future {
      val userAgent = Random.shuffle(UserAgentUtil.userAgents).head
      val result = Http(url)
        .header("User-Agent", userAgent)
        .option(HttpOptions.readTimeout(4000)).asString
      if (result.is2xx) {
        Some(result.body)
      } else {
        log.error(s"Error calling Url: ${url} with response: ${result}")
        None
      }
    }
  }

}
