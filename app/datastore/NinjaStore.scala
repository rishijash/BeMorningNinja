package datastore

import java.io.{File, FileInputStream}
import java.util

import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.firestore.{FirestoreOptions, QueryDocumentSnapshot}
import javax.inject.Inject
import org.slf4j.LoggerFactory
import play.api.Configuration
import java.io.BufferedWriter
import java.io.FileWriter

import scala.collection.JavaConversions._
import java.util.UUID.randomUUID
import java.util.Calendar

import models.{Account, Ninja}

class NinjaStore {

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  private val accountsCollection = "accounts"
  private val usernameKey = "username"
  private val genreKey = "genre"
  private val thumbsUpCountKey = "thumbsUp"
  private val sleepyCountKey = "sleepyCount"
  private val gymCountKey = "gymCount"
  private val accountPictureKey = "accountPicture"
  private val accountSummaryKey = "accountSummary"
  private val backupAudioKey = "backupAudio"

  private val ninjaCollection = "ninjas"
  private val ninjaIdKey = "ninjaId"
  private val lastAlarmKey = "lastAlarm"
  private val lastAlarmUsernameKey = "lastAlarmUsername"

  private val privateKeyId = scala.util.Properties.envOrElse("private_key_id", "")
  private val privateKey = scala.util.Properties.envOrElse("private_key", "")
  private val clientEmail = scala.util.Properties.envOrElse("client_email", "")
  private val clientId = scala.util.Properties.envOrElse("client_id", "")
  private val clientCertUrl = scala.util.Properties.envOrElse("client_x509_cert_url", "")

  private val createKeyFileData =
    s"""
       |{
       |  "type": "service_account",
       |  "project_id": "bemorningninja-251619",
       |  "private_key_id": "${privateKeyId}",
       |  "private_key": "${privateKey}",
       |  "client_email": "${clientEmail}",
       |  "client_id": "${clientId}",
       |  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
       |  "token_uri": "https://oauth2.googleapis.com/token",
       |  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
       |  "client_x509_cert_url": "${clientCertUrl}"
       |}
    """.stripMargin

  final val tempFile = File.createTempFile("beMorningNinjaKey", ".json")
  final val writer: BufferedWriter = new BufferedWriter(new FileWriter(tempFile.getAbsolutePath))
  writer.write(createKeyFileData)
  writer.close()

  private val firebaseClient = FirestoreOptions.newBuilder()
    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(tempFile.getAbsolutePath)))
    .build().getService

  def getAccounts(): Option[List[Account]] = {
    try {
      val allData = firebaseClient.collection(accountsCollection).get().get()
      val allDocuments = allData.getDocuments.toList
      val usernamesData = allDocuments.map(d => {
        val username = d.get(usernameKey).toString
        val genre = Option(d.get(genreKey)).map(_.toString).filter(_.nonEmpty)
        val gymCount = Option(d.get(gymCountKey)).map(_.toString.toInt)
        val sleepyCount = Option(d.get(sleepyCountKey)).map(_.toString.toInt)
        val thumbsupCount = Option(d.get(thumbsUpCountKey)).map(_.toString.toInt)
        val accountPicture = d.get(accountPictureKey).toString
        val accountSummary = Option(d.get(accountSummaryKey)).map(_.toString).filter(_.nonEmpty).getOrElse("")
        val backupAudio = Option(d.get(backupAudioKey)).map(_.toString).filter(_.nonEmpty)

        Account(d.getId, username, accountPicture, accountSummary, genre, thumbsupCount, sleepyCount, gymCount, backupAudio)
      })
      Some(usernamesData)
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in getAccounts.")
        None
    }
  }

  def getAccount(username: String): Option[Account] = {
    try {
      val accountData = firebaseClient.collection(accountsCollection).whereEqualTo(usernameKey, username).get().get()
      val accountDocument = accountData.getDocuments.toList.headOption
      accountDocument.map(d => {
        val genre = Option(d.get(genreKey)).map(_.toString).filter(_.nonEmpty)
        val gymCount = Option(d.get(gymCountKey)).map(_.toString.toInt)
        val sleepyCount = Option(d.get(sleepyCountKey)).map(_.toString.toInt)
        val thumbsupCount = Option(d.get(thumbsUpCountKey)).map(_.toString.toInt)
        val accountPicture = d.get(accountPictureKey).toString
        val accountSummary = Option(d.get(accountSummaryKey)).map(_.toString).filter(_.nonEmpty).getOrElse("")
        val backupAudio = Option(d.get(backupAudioKey)).map(_.toString).filter(_.nonEmpty)

        Account(d.getId, username, accountPicture, accountSummary, genre, thumbsupCount, sleepyCount, gymCount, backupAudio)
      })
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in getAccount for username: ${username}.")
        None
    }
  }

  def updateAccount(username: String, thumbsUp: Boolean, gym: Boolean, sleepy: Boolean): Boolean = {
    try {
      val maybeAccount = getAccount(username)
      if (maybeAccount.isDefined) {
        val gymCount = maybeAccount.get.gymCount.getOrElse(0) + (if (gym) 1 else 0)
        val sleepyCount = maybeAccount.get.sleepyCount.getOrElse(0) + (if (sleepy) 1 else 0)
        val thumbsupCount = maybeAccount.get.thumbsupCount.getOrElse(0) + (if (thumbsUp) 1 else 0)
        val docData = (for {
          genre <- maybeAccount.get.genre
        } yield {
          new util.HashMap[String, Any](Map(usernameKey -> username, genreKey -> genre, thumbsUpCountKey -> thumbsupCount, gymCountKey -> gymCount, sleepyCountKey -> sleepyCount, accountPictureKey -> maybeAccount.get.accountPicture, accountSummaryKey -> maybeAccount.get.accountSummary, backupAudioKey -> maybeAccount.get.backupVideo.getOrElse("")))
        }).getOrElse({
          new util.HashMap[String, Any](Map(usernameKey -> username, thumbsUpCountKey -> thumbsupCount, gymCountKey -> gymCount, sleepyCountKey -> sleepyCount, accountPictureKey -> maybeAccount.get.accountPicture, accountSummaryKey -> maybeAccount.get.accountSummary, backupAudioKey -> maybeAccount.get.backupVideo.getOrElse("")))
        })
        firebaseClient.collection(accountsCollection).document(maybeAccount.get.dataId).set(docData)
        true
      } else {
        false
      }
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in updating account for username: ${username}")
        false
    }
  }

  def updateAccountAudio(username: String, newBackupAudio: String): Boolean = {
    try {
      val maybeAccount = getAccount(username)
      if (maybeAccount.isDefined) {
        val docData = (for {
          genre <- maybeAccount.get.genre
        } yield {
          new util.HashMap[String, Any](Map(usernameKey -> username, genreKey -> genre, thumbsUpCountKey -> maybeAccount.get.thumbsupCount.getOrElse(0), gymCountKey -> maybeAccount.get.gymCount.getOrElse(0), sleepyCountKey -> maybeAccount.get.sleepyCount.getOrElse(0), accountPictureKey -> maybeAccount.get.accountPicture, accountSummaryKey -> maybeAccount.get.accountSummary, backupAudioKey -> newBackupAudio))
        }).getOrElse({
          new util.HashMap[String, Any](Map(usernameKey -> username, thumbsUpCountKey -> maybeAccount.get.thumbsupCount.getOrElse(0), gymCountKey -> maybeAccount.get.gymCount.getOrElse(0), sleepyCountKey -> maybeAccount.get.sleepyCount.getOrElse(0), accountPictureKey -> maybeAccount.get.accountPicture, accountSummaryKey -> maybeAccount.get.accountSummary, backupAudioKey -> newBackupAudio))
        })
        firebaseClient.collection(accountsCollection).document(maybeAccount.get.dataId).set(docData)
        true
      } else {
        false
      }
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in updating account for username: ${username}")
        false
    }
  }

  def addNinja(ninjaId: String, maybeLastAlarm: Option[String], maybeLastUsername: Option[String]): Boolean = {
    try {
      val created = Calendar.getInstance().getTime().toString()
      val docData = (for {
        lastAlarm <- maybeLastAlarm
        lastUsername <- maybeLastUsername
      } yield {
        new util.HashMap[String, Any](Map(ninjaIdKey -> ninjaId,
          lastAlarmKey -> lastAlarm, lastAlarmUsernameKey -> lastUsername))
      }).getOrElse({
        new util.HashMap[String, Any](Map(ninjaIdKey -> ninjaId))
      })
      val id = randomUUID().toString
      firebaseClient.collection(accountsCollection).document(id).set(docData)
      true
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in creating ninja for ninjaId: ${ninjaId}")
        false
    }
  }

  def getNinja(ninjaId: String): Option[Ninja] = {
    try {
      val ninjaData = firebaseClient.collection(ninjaCollection).whereEqualTo(ninjaIdKey, ninjaId).get().get()
      val ninjaDocuments = ninjaData.getDocuments.toList.headOption
      ninjaDocuments.map(d => {
        val dataId = d.getId
        val lastAlarm = Option(d.getData.get(lastAlarmKey)).map(_.toString).filter(_.nonEmpty)
        val lastUsername = Option(d.getData.get(lastAlarmUsernameKey)).map(_.toString).filter(_.nonEmpty)
        Ninja(dataId, ninjaId, lastAlarm, lastUsername)
      })
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in getAccounts.")
        None
    }
  }

  def updateNinja(ninjaId: String, maybeLastAlarm: Option[String], maybeLastUsername: Option[String]): Boolean = {
    try {
      val maybeNinja = getNinja(ninjaId)
      if (maybeNinja.isDefined) {
        val docData = (for {
          lastAlarm <- maybeLastAlarm
          lastUsername <- maybeLastUsername
        } yield {
          new util.HashMap[String, Any](Map(ninjaIdKey -> ninjaId,
            lastAlarmKey -> lastAlarm, lastAlarmUsernameKey -> lastUsername))
        }).getOrElse({
          new util.HashMap[String, Any](Map(ninjaIdKey -> ninjaId))
        })
        firebaseClient.collection(accountsCollection).document(maybeNinja.get.dataId).set(docData)
        true
      } else {
        false
      }
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in updating ninja for ninjaId: ${ninjaId}")
        false
    }
  }

}
