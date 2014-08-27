package edu.illinois.i3.apps.branthouston.twitterclient

import com.typesafe.scalalogging.slf4j.Logging
import twitter4j.{User, TwitterFactory}
import scala.collection.JavaConversions._

import scala.reflect.io.File

object TWSearch extends App with Logging {

  val twitter = TwitterFactory.getSingleton
  val MAX_RETRIES = 5

  def searchTwitter(keyword: String) = {
    logger.info(s"Running query: $keyword")
    val users = (1 to 50).flatMap {
      case page =>
        var result = Seq.empty[User]
        var attempt = 0
        do {
          try {
            attempt += 1
            result = twitter.searchUsers(keyword, page)
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

  val champaignUsers = searchTwitter("champaign")
  val urbanaUsers = searchTwitter("urbana")
  val chambanaUsers = searchTwitter("chambana")
  val uniqueUsers = Set(champaignUsers ++ urbanaUsers ++ chambanaUsers: _*)

  logger.info(s"${uniqueUsers.size} unique users found")

  val sortedUsers = uniqueUsers.toSeq.sortWith(_.getFollowersCount > _.getFollowersCount)

  val sb = new StringBuilder
  sb.append("screen_name\tfollowers\tfriends\ttweets\tlocation\ttimezone\tutc_offset\tcreated\tgeo_enabled\n")
  for (u <- sortedUsers) {
    sb.append(s"${u.getScreenName}\t${u.getFollowersCount}\t${u.getFriendsCount}\t${u.getStatusesCount}\t${u.getLocation}\t${u.getTimeZone}\t${u.getUtcOffset}\t${u.getCreatedAt.toString}\t${u.isGeoEnabled}\n")
  }

  File("tw_users.tsv").writeAll(sb.toString())
}
