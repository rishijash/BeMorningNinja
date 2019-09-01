package clients.managers

import clients.instagram.{InstagramClient, InstagramProfile}
import datastore.NinjaStore
import models.{GetProfileRes, GetProfilesRes, Profile, SelectedMedia}
import util.HtmlUtil

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class NinjaManager {

  private val instagramClient = new InstagramClient()
  private lazy val store = new NinjaStore()

  def getProfile(username: String): Future[GetProfileRes] = {
    instagramClient.getProfile(username).map(instagramProfile => {
      val profile = instagramProfile.map(i => toProfile(i, true))
      GetProfileRes(profile)
    })
  }

  def getProfiles(): Future[GetProfilesRes] = {
    val usernames = store.getAccounts().getOrElse(List.empty)
    val usernamesFutureList = usernames.map(user => {
      instagramClient.getProfile(user)
    })
    val usernamesFuture = Future.sequence(usernamesFutureList)
    usernamesFuture.map(usernamesRes => {
      val profiles = usernamesRes.flatten.map(toProfile(_))
      GetProfilesRes(profiles = profiles)
    })
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
        videoLink = if (includeVideoLink) getVideLink(postUrl) else None
      )
    })
  }

  private def getVideLink(postUrl: String): Option[String] = {
    val htmlFuture = HtmlUtil.getHtmlFromUrl(postUrl)
    val htmlRes = Await.result(htmlFuture, 2.seconds)
    htmlRes.map(html => {
      val startKeyIndex = html.indexOf("https://scontent.cdninstagram.com/v")
      val subStringHtml = html.substring(startKeyIndex, html.length)
      val endKeyIndex = subStringHtml.indexOf("\"")
      val url = subStringHtml.substring(0, endKeyIndex).replaceAll("\\\\u0026", "&")
      url
    })
  }

  private def getInstagramPostUrl(shortcode: String): String = {
    s"${instagramClient.baseUrl}p/${shortcode}/"
  }

}
