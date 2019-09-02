package util

import org.slf4j.LoggerFactory
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object HtmlUtil {

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  def getHtmlFromUrl(url: String): Future[Option[String]] = {
    sendRequest(url).map(htmlCode => {
      htmlCode
    })
  }

  private def sendRequest(url: String): Future[Option[String]] = {
    Future {
      val result = Http(url)
        .option(HttpOptions.readTimeout(2000)).asString
      if (result.is2xx) {
        Some(result.body)
      } else {
        log.error(s"Error calling Url: ${url} with response: ${result}")
        None
      }
    }
  }

}
