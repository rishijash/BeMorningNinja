package util

import org.slf4j.LoggerFactory
import scalaj.http.{Http, HttpOptions}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object HtmlUtil {

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  def getHtmlFromUrl(url: String): Future[Option[String]] = {
    ReqBinUtil.sendRequest(url).map(htmlRes => {
      htmlRes.right.toOption.map(_.body)
    })
  }

}
