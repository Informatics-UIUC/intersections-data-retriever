package edu.illinois.i3.apps.branthouston.twitter

import com.typesafe.scalalogging.LazyLogging
import twitter4j.User
import scala.collection.JavaConversions._

import scala.reflect.io.File

object TWSearchUsers extends App with TwitterAPI with LazyLogging {

  val MAX_RETRIES = 5

  def searchUsers(query: String) = {
    logger.info(s"Running query: $query")
    val users = (1 to 50).flatMap {
      case page =>
        var result = Seq.empty[User]
        var attempt = 0
        do {
          try {
            attempt += 1
            result = twitter.searchUsers(query, page)
          } catch {
            case e: Exception =>
              Thread.sleep(500)
              logger.error(s"Attempt $attempt: ${e.getMessage}")
          }
        } while (result.isEmpty && attempt < MAX_RETRIES)
        result
    }

    users
  }

  val champaignUsers = searchUsers("champaign")
  val urbanaUsers = searchUsers("urbana")
  val chambanaUsers = searchUsers("chambana")
  val uniqueUsers = Set(champaignUsers ++ urbanaUsers ++ chambanaUsers: _*)

  logger.info(s"${uniqueUsers.size} unique users found")

  val sortedUsers = uniqueUsers.toSeq.sortWith(_.getFollowersCount > _.getFollowersCount)

  val sb = new StringBuilder
  sb.append("screen_name\tfollowers\tfriends\ttweets\tlocation\ttimezone\tutc_offset\tcreated\tgeo_enabled\n")
  for (u <- sortedUsers) {
    sb.append(s"${u.getScreenName}\t${u.getFollowersCount}\t${u.getFriendsCount}\t${u.getStatusesCount}\t${u.getLocation}\t${u.getTimeZone}\t${u.getUtcOffset}\t${u.getCreatedAt.toString}\t${u.isGeoEnabled}\n")
  }

  val saveAs = io.StdIn.readLine("Save as: ")
  File(saveAs).writeAll(sb.toString())
}
