package util

import java.io.File
import java.util.Scanner

import scala.util.control.NonFatal

object UserAgentUtil {

  val androidAgents = new File("conf/userAgents/Android+Webkit+Browser.txt")
  val chromeAgents = new File("conf/userAgents/Chrome.txt")
  val edgeAgents = new File("conf/userAgents/Edge.txt")
  val firefoxAgents = new File("conf/userAgents/Firefox.txt")
  val internetExplorerAgents = new File("conf/userAgents/Internet+Explorer.txt")
  val operaAgents = new File("conf/userAgents/Opera.txt")
  val safariAgents = new File("conf/userAgents/Safari.txt")

  val userAgents = {
    fileToList(androidAgents) ++
    fileToList(chromeAgents) ++
    fileToList(edgeAgents) ++
    fileToList(firefoxAgents) ++
    fileToList(internetExplorerAgents) ++
    fileToList(operaAgents) ++
    fileToList(safariAgents)
  }

  private def fileToList(file: File): List[String] = {
    try {
      val sc = new Scanner(file)
      val res = scala.collection.mutable.ListBuffer.empty[String]
      while (sc.hasNextLine())
        res += sc.nextLine()
      res.toList.filter(_.nonEmpty)
    } catch {
      case NonFatal(e) => {
        val t = e
        List.empty
      }
    }
  }

}
