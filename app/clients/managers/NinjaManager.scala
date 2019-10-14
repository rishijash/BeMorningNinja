package clients.managers

import clients.Response
import clients.instagram.{InstagramClient, InstagramProfile}
import com.google.inject.Inject
import datastore.NinjaStore
import models.{Account, AccountsResponse, GetProfileRes, GetProfilesRes, Ninja, Profile, SelectedMedia}
import play.api.libs.ws.WSClient
import util.UserAgentUtil

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

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
        val sortedAccounts = accounts.get.sortWith(sortByPoint)
        Right(AccountsResponse(sortedAccounts))
      } else {
        Left(models.Error("ACCOUNT_ERROR", s"ACCOUNTS not found."))
      }
    }
  }

  def getAlexaProfile(): Future[Either[models.Error, GetProfileRes]] = {
    getAccounts().flatMap(_.fold(
      error => Future.successful(Left(error)),
      accounts => {
        val sortedAccountsList = accounts.accounts.filterNot(_.genre.getOrElse("").equalsIgnoreCase("youtuber")).take(4)
        // To give first profile more preference
        val listWithPriority = List(
          sortedAccountsList(0),
          sortedAccountsList(0)) ++ sortedAccountsList
        val selectedAccount = Random.shuffle(listWithPriority).head
        getProfile(selectedAccount.username)
      }
    ))
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

  def getProfiles(withContent: Option[Boolean], withSelectedContent: Option[Boolean]): Future[Either[models.Error, GetProfilesRes]] = {
    val usernamesData = store.getAccounts().getOrElse(List.empty)
    if (withContent.getOrElse(true)) {
      val usernamesFutureList = usernamesData.map(data => instagramClient.getProfile(data.username))
      val usernamesFuture = Future.sequence(usernamesFutureList)
      usernamesFuture.map(usernamesRes => {
        val profiles = usernamesRes.flatMap(usernameRes => {
          val account = usernamesData.find(_.username == usernameRes.right.toOption.map(_.graphql.user.username).getOrElse(""))
          val includeVideoLink = withSelectedContent.getOrElse(true)
          val maybeProfile = usernameRes.right.toOption.map(instaProf => toProfile(instaProf, account, includeVideoLink))
          maybeProfile
        })
        // Sort Profiles
        var sortedProfiles = profiles.sortWith(sortByPoint)
        if(sortedProfiles.isEmpty) {
          sortedProfiles = usernamesData.map(account => {
            Profile(
              account.username,
              Some(account),
              "instagram.com/"+account.username,
              account.genre,
              Some(account.accountSummary),
              Some(account.accountPicture),
              Some(
                SelectedMedia(
                  account.backupVideo.getOrElse(""),
                  account.backupVideo.getOrElse(""),
                  account.backupVideo
                )
              ),
              None
            )
          }).sortWith(sortByPoint)
        }
        val res = GetProfilesRes(profiles = sortedProfiles)
        Right(res)
      })
    } else {
      val profiles = usernamesData.map(d => toProfile(d.username, Some(d), d.genre))
      // Sort Profiles
      val sortedProfiles = profiles.sortWith(sortByPoint)
      Future.successful(Right(GetProfilesRes(profiles = sortedProfiles)))
    }
  }

  private def sortByPoint(p1: Profile, p2: Profile) = {
    val p1Points = p1.account.flatMap(_.gymCount).getOrElse(0) - p1.account.flatMap(_.sleepyCount).getOrElse(0)
    val p2Points = p2.account.flatMap(_.gymCount).getOrElse(0) - p2.account.flatMap(_.sleepyCount).getOrElse(0)
    p1Points > p2Points
  }

  private def sortByPoint(a1: Account, a2: Account) = {
    val a1Points = a1.gymCount.getOrElse(0) - a1.sleepyCount.getOrElse(0)
    val a2Points = a2.gymCount.getOrElse(0) - a2.sleepyCount.getOrElse(0)
    a1Points > a2Points
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
      // If latest video was posted in 30 minutes use it else use random one
      val selected = if (unixTime - l.node.taken_at_timestamp <= 1800) {
        l
      } else {
        Random.shuffle(list).head
      }
      val postUrl = getInstagramPostUrl(selected.node.shortcode)
      SelectedMedia(
        displayUrl = selected.node.display_url,
        instagramPostUrl = postUrl,
        videoLink = None
      )
    })
  }

  private def getBestVideo(instagramProfile: InstagramProfile, includeVideoLink: Boolean = false): Option[SelectedMedia] = {
    val list = instagramProfile.graphql.user.edge_felix_video_timeline.edges.filter(_.node.is_video == true)
    val unixTime = System.currentTimeMillis / 1000L
    val latest = Option(list).filter(_.nonEmpty).map(_.minBy(e => math.abs(unixTime - e.node.taken_at_timestamp)))
    latest.map(l => {
      // If latest video was posted in 30 minutes use it else use random one
      val selected = if (unixTime - l.node.taken_at_timestamp <= 1800) {
        l
      } else {
        Random.shuffle(list).head
      }
      val postUrl = getInstagramPostUrl(selected.node.shortcode)
      SelectedMedia(
        displayUrl = selected.node.display_url,
        instagramPostUrl = postUrl,
        videoLink = if (includeVideoLink) instagramClient.getVideoLinkWebSync(postUrl) else None
      )
    })
  }

  private def getInstagramPostUrl(shortcode: String): String = {
    s"${instagramClient.baseUrl}p/${shortcode}/"
  }

}
