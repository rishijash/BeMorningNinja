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

class NinjaStore {

  private val log = LoggerFactory.getLogger(this.getClass.getName)

  private val accountsCollection = "accounts"
  private val usernameKey = "username"

  val hostKey = "host"
  val totalParamsCountKey = "totalParamsCount"
  val safeParamsCountKey = "safeParamsCount"
  val createdKey = "created"

  private val alarmsCollection = "alarms"

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

  final val tempFile = File.createTempFile("beMorningNinjaKey",".json")
  final val writer: BufferedWriter = new BufferedWriter(new FileWriter(tempFile.getAbsolutePath))
  writer.write(createKeyFileData)
  writer.close()

  private val firebaseClient = FirestoreOptions.newBuilder()
    .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(tempFile.getAbsolutePath)))
    .build().getService

  def createMetrics(host: String, totalParamsCount: Int, safeParamsCount: Int = 0): Boolean = {
    try {
      val created = Calendar.getInstance().getTime().toString()
      val docData = new util.HashMap[String, Any](Map(hostKey -> host,
        totalParamsCountKey -> totalParamsCount, safeParamsCountKey -> safeParamsCount, createdKey -> created))
      val id = randomUUID().toString
      firebaseClient.collection(accountsCollection).document(id).set(docData)
      true
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in creating metrics for host: ${host}, totalParamsCount: ${totalParamsCount}, safeParamsCount: ${safeParamsCount}")
        false
    }
  }

  def getAccounts(): Option[List[String]] = {
    try {
      val allData = firebaseClient.collection(accountsCollection).get().get()
      val allDocuments = allData.getDocuments.toList
      val usernames = allDocuments.map(doc => {
       doc.getData.get(usernameKey).toString
      })
      Option(usernames.distinct).filter(_.nonEmpty)
    } catch {
      case e: Exception =>
        log.error(s"Exception: ${e.getMessage} in getAccounts.")
        None
    }
  }

}
