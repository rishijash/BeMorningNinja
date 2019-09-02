package models

import play.api.libs.json.Json

case class Account (
  dataId: String,
  username: String,
  genre: Option[String],
  thumbsupCount: Option[Int],
  sleepyCount: Option[Int],
  gymCount: Option[Int]
)

object Account {
  implicit val accountFormat = Json.format[Account]
}
