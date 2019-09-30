package util

import scala.util.Random

object TimeoutUtil {

  val acceptedTimeoutInSec = List(
    15,
    25,
    30
  )

  def getRandomTimeoutInSec = Random.shuffle(acceptedTimeoutInSec).headOption.getOrElse(10)


}
