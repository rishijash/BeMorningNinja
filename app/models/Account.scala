package models

import play.api.libs.json.Json

case class Account (
  dataId: String,
  username: String,
  accountPicture: String,
  accountSummary: String,
  genre: Option[String],
  thumbsupCount: Option[Int],
  sleepyCount: Option[Int],
  gymCount: Option[Int],
  backupVideo: Option[String]
)

object Account {
  implicit val accountFormat = Json.format[Account]
}
