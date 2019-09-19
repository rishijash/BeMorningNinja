package clients.managers

import clients.Response
import clients.instagram.{InstagramClient, InstagramProfile}
import com.google.inject.Inject
import datastore.NinjaStore
import models.{Account, AccountsResponse, GetProfileRes, GetProfilesRes, Ninja, Profile, SelectedMedia}
import play.api.libs.ws.WSClient

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NinjaManager @Inject()(implicit ws: WSClient) {

  private val instagramClient = new InstagramClient()
  private lazy val store = new NinjaStore()

  def getProfile(username: String): Future[Either[models.Error, GetProfileRes]] = {
    instagramClient.getProfile(username).map(_.fold(
      error => Left(error),
      instagramProfile => {
        val account = store.getAccounts().flatMap(_.find(_.username == username))
        val profile = toProfile(instagramProfile, account, true)
        val res = GetProfileRes(profile)
        Right(res)
      }
    ))
  }

  def getAccounts(): Future[Either[models.Error, AccountsResponse]] = {
    Future {
      val accounts = store.getAccounts()
      if(accounts.isDefined) {
        Right(AccountsResponse(accounts.get))
      } else {
        Left(models.Error("ACCOUNT_ERROR", s"ACCOUNTS not found."))
      }
    }
  }

  def updateAccount(username: String, thumbsUp: Boolean, gym: Boolean, sleepy: Boolean): Future[Either[models.Error, Boolean]] = {
    Future {
      val res = store.updateAccount(username, thumbsUp, gym, sleepy)
      if (res) {
        Right(res)
      } else {
        Left(models.Error("ACCOUNT_ERROR", s"ACCOUNT not updated."))
      }
    }
  }

  def getProfiles(withContent: Option[Boolean]): Future[Either[models.Error, GetProfilesRes]] = {
    val usernamesData = store.getAccounts().getOrElse(List.empty)
    if (withContent.getOrElse(true)) {
      val usernamesFutureList = usernamesData.map(_.username).map(user => instagramClient.getProfile(user))
      val usernamesFuture = Future.sequence(usernamesFutureList)
      usernamesFuture.map(usernamesRes => {
        val profiles = usernamesRes.flatMap(usernameRes => {
          val account = usernamesData.find(_.username == usernameRes.right.toOption.map(_.graphql.user.username).getOrElse(""))
          usernameRes.right.toOption.map(instaProf => toProfile(instaProf, account))
        })
        val res = GetProfilesRes(profiles = profiles)
        Right(res)
      })
    } else {
      val profiles = usernamesData.map(d => toProfile(d.username, Some(d), d.genre))
      Future.successful(Right(GetProfilesRes(profiles = profiles)))
    }
  }

  def getNinja(ninjaId: String): Future[Either[models.Error, Ninja]] = {
    Future {
      val maybeNinja = store.getNinja(ninjaId)
      if (maybeNinja.isDefined) {
        Right(maybeNinja.get)
      } else {
        Left(models.Error("NINJA_ERROR", s"NINJA not found for NinjaId: ${ninjaId}"))
      }
    }
  }

  def addNinja(ninjaId: String, maybeLastAlarm: Option[String], maybeLastUsername: Option[String]): Future[Either[models.Error, Boolean]] = {
    Future {
      val res = store.addNinja(ninjaId, maybeLastAlarm, maybeLastUsername)
      if (res) {
        Right(res)
      } else {
        Left(models.Error("NINJA_ERROR", s"NINJA not created for NinjaId: ${ninjaId}"))
      }
    }
  }

  def updateNinja(ninjaId: String, maybeLastAlarm: Option[String], maybeLastUsername: Option[String]): Future[Either[models.Error, Boolean]] = {
    Future {
      val res = store.updateNinja(ninjaId, maybeLastAlarm, maybeLastUsername)
      if (res) {
        Right(res)
      } else {
        Left(models.Error("NINJA_ERROR", s"NINJA not updated for NinjaId: ${ninjaId}"))
      }
    }
  }

  private def toProfile(instagramProfile: InstagramProfile, account: Option[Account], includeVideoLink: Boolean = false): Profile = {
    Profile(
      username = instagramProfile.graphql.user.username,
      account = account,
      profileUrl = s"${instagramClient.baseUrl}${instagramProfile.graphql.user.username}",
      genre = None,
      summary = instagramProfile.graphql.user.biography,
      profilePic = instagramProfile.graphql.user.profile_pic_url,
      selectedImageUrl = getBestImage(instagramProfile),
      selectedVideoUrl = getBestVideo(instagramProfile, includeVideoLink)
    )
  }

  private def toProfile(username: String, account: Option[Account], genre: Option[String]): Profile = {
    Profile(
      username = username,
      account = account,
      profileUrl = s"${instagramClient.baseUrl}${username}",
      genre = genre,
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
