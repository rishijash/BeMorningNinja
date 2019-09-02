package clients.managers

import clients.instagram.{InstagramClient, InstagramProfile}
import com.google.inject.Inject
import datastore.NinjaStore
import models.{GetProfileRes, GetProfilesRes, Profile, SelectedMedia}
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NinjaManager @Inject()(implicit ws: WSClient) {

  private val instagramClient = new InstagramClient()
  private lazy val store = new NinjaStore()

  def getProfile(username: String): Future[Either[models.Error, GetProfileRes]] = {
    instagramClient.getInstagramProfileWeb(username).map(_.fold(
      error => Left(error),
      instagramProfile => {
        val profile = toProfile(instagramProfile, true)
        val res = GetProfileRes(profile)
        Right(res)
      }
    ))
  }

  def getProfiles(withContent: Option[Boolean]): Future[Either[models.Error, GetProfilesRes]] = {
    val usernames = store.getAccounts().getOrElse(List.empty)
    if (withContent.getOrElse(true)) {
      val usernamesFutureList = usernames.map(user => instagramClient.getInstagramProfileWeb(user))
      val usernamesFuture = Future.sequence(usernamesFutureList)
      usernamesFuture.map(usernamesRes => {
        val profiles = usernamesRes.flatMap(_.right.toOption).map(toProfile(_))
        val res = GetProfilesRes(profiles = profiles)
        Right(res)
      })
    } else {
      val profiles = usernames.map(toProfile(_))
      Future.successful(Right(GetProfilesRes(profiles = profiles)))
    }
  }

  private def toProfile(instagramProfile: InstagramProfile, includeVideoLink: Boolean = false): Profile = {
    Profile(
      username = instagramProfile.graphql.user.username,
      profileUrl = s"${instagramClient.baseUrl}${instagramProfile.graphql.user.username}",
      summary = instagramProfile.graphql.user.biography,
      profilePic = instagramProfile.graphql.user.profile_pic_url,
      selectedImageUrl = getBestImage(instagramProfile),
      selectedVideoUrl = getBestVideo(instagramProfile, includeVideoLink)
    )
  }

  private def toProfile(username: String): Profile = {
    Profile(
      username = username,
      profileUrl = s"${instagramClient.baseUrl}${username}",
      summary = None,
      profilePic = None,
      selectedImageUrl = None,
      selectedVideoUrl = None
    )
  }

  private def getBestImage(instagramProfile: InstagramProfile): Option[SelectedMedia] = {
    val list = instagramProfile.graphql.user.edge_owner_to_timeline_media.edges.filter(_.node.is_video == false)
    val unixTime = System.currentTimeMillis / 1000L
    val latest = Option(list).filter(_.nonEmpty).map(_.minBy(e => math.abs(unixTime - e.node.taken_at_timestamp)))
    latest.map(l => {
      SelectedMedia(
        displayUrl = l.node.display_url,
        instagramPostUrl = getInstagramPostUrl(l.node.shortcode),
        videoLink = None
      )
    })
  }

  private def getBestVideo(instagramProfile: InstagramProfile, includeVideoLink: Boolean = false): Option[SelectedMedia] = {
    val list = instagramProfile.graphql.user.edge_owner_to_timeline_media.edges.filter(_.node.is_video == true)
    val unixTime = System.currentTimeMillis / 1000L
    val latest = Option(list).filter(_.nonEmpty).map(_.minBy(e => math.abs(unixTime - e.node.taken_at_timestamp)))
    latest.map(l => {
      val postUrl = getInstagramPostUrl(l.node.shortcode)
      SelectedMedia(
        displayUrl = l.node.display_url,
        instagramPostUrl = postUrl,
        videoLink = if (includeVideoLink) instagramClient.getVideLinkWebSync(postUrl) else None
      )
    })
  }

  private def getInstagramPostUrl(shortcode: String): String = {
    s"${instagramClient.baseUrl}p/${shortcode}/"
  }

}
