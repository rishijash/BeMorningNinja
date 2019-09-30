package util

import scala.util.Random

object TimeoutUtil {

  val acceptedTimeoutInSec = List(
    30,
    45,
    60
  )

  def getRandomTimeoutInSec = Random.shuffle(acceptedTimeoutInSec).headOption.getOrElse(30)


}
