package util

import scalaj.http.{Http, HttpOptions}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

object HtmlUtil {

  def getHtmlFromUrl(url: String): Future[Option[String]] = {
    sendRequest(url).map(htmlCode => {
      htmlCode
    })
  }

  private def sendRequest(url: String): Future[Option[String]] = {
    Future {
      val result = Http(url)
        .option(HttpOptions.readTimeout(2000)).asString
      if (result.code == 200) {
        Some(result.body)
      } else {
        None
      }
    }
  }

}
