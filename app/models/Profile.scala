package models

import play.api.libs.json.Json

case class SelectedMedia (
  displayUrl: String,
  instagramPostUrl: String,
  videoLink: Option[String]
)

case class Profile (
  username: String,
  account: Option[Account],
  profileUrl: String,
  genre: Option[String],
  summary: Option[String],
  profilePic: Option[String],
  selectedVideoUrl: Option[SelectedMedia],
  selectedImageUrl: Option[SelectedMedia]
)

object SelectedMedia {
  implicit val selectedMediaFormat = Json.format[SelectedMedia]
}

object Profile {
  implicit val profileFormat = Json.format[Profile]
}