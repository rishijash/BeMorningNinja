package clients.instagram

import play.api.libs.json.Json

case class Node (
  __typename: String,
  id: String,
  shortcode: String,
  edge_media_to_comment: Option[Count],
  taken_at_timestamp: Long,
  display_url: String,
  edge_liked_by: Option[Count],
  edge_media_preview_like: Option[Count],
  is_video: Boolean,
  video_view_count: Option[Int]
)

case class Edge (
  node: Node
)

case class OwnerTimeline (
  count: Int,
  edges: List[Edge]
)

case class Count (
  count: Int
)

case class User (
  username: String,
  biography: Option[String],
  external_url: Option[String],
  edge_followed_by: Option[Count],
  edge_follow: Option[Count],
  full_name: Option[String],
  business_category_name: Option[String],
  is_verified: Option[Boolean],
  profile_pic_url: Option[String],
  profile_pic_url_hd: Option[String],
  edge_owner_to_timeline_media: OwnerTimeline
)

case class Graphql (
     user: User
)

case class InstagramProfile (
  logging_page_id: String,
  graphql: Graphql
)


object Count {
  implicit val countFormat = Json.format[Count]
}

object Node {
  implicit val nodeFormat = Json.format[Node]
}

object Edge {
  implicit val edgeFormat = Json.format[Edge]
}


object OwnerTimeline {
  implicit val ownerTimelineFormat = Json.format[OwnerTimeline]
}

object User {
  implicit val userFormat = Json.format[User]
}

object Graphql {
  implicit val graphqlFormat = Json.format[Graphql]
}

object InstagramProfile {
  implicit val instagramProfileFormat = Json.format[InstagramProfile]
}
