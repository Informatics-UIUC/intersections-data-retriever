package edu.illinois.i3.apps.branthouston.twitter

import java.io.IOException

import com.typesafe.scalalogging.slf4j.Logging
import twitter4j.{TwitterException, Paging, Status}
import scala.collection.JavaConversions._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

import scala.reflect.io.File

object TWGetUserTimeline extends App with TwitterAPI with Logging {

  def getUserTimeline(screenName: String) = {
    print("+")
    var userTimeline = Seq.empty[Status]
    var pageId = 1
    val MAX_RETRIES = 5
    var timeline = twitter.getUserTimeline(screenName, new Paging(pageId, 200))
    print(".")

    while (timeline.nonEmpty) {
      userTimeline ++= timeline
      val maxId = timeline.minBy(_.getId).getId
      pageId += 1

      if (timeline.getRateLimitStatus.getRemaining < 2) {
        val sleepFor = timeline.getRateLimitStatus.getSecondsUntilReset
        logger.warn("Rate limit reached - sleeping for {} sec", sleepFor.toString)
        print("S")
        //Thread.sleep((sleepFor + 30) * 1000)
        Thread.sleep(TWITTER_RATE_LIMIT_WINDOW_SEC * 1000)
        logger.info("Resuming...")
      }

      var attempt = 0
      timeline = null
      while (timeline == null && attempt < MAX_RETRIES) {
        try {
          attempt += 1
          timeline = twitter.getUserTimeline(screenName, new Paging(pageId, 200, 1, maxId - 1))
          print(".")
        } catch {
          case e @ (_: TwitterException | _: IOException) if attempt < MAX_RETRIES =>
            print("E")
            logger.error(s"Attempt $attempt: ${e.getMessage}")
            Thread.sleep(500 * attempt)
        }
      }
    }

    userTimeline
  }

  val user = readLine("Twitter handle: ")
  val tweets = getUserTimeline(user)

  val json = pretty(render(tweets.map(t =>
      ("id" -> t.getId) ~
      ("text" -> t.getText) ~
      ("createdAt" -> t.getCreatedAt.toString) ~
      ("source" -> t.getSource) ~
      ("lang" -> t.getLang) ~
      ("retweetCount" -> t.getRetweetCount) ~
      ("userName" -> t.getUser.getName) ~
      ("screenName" -> t.getUser.getScreenName)
  )))

  val saveAs = readLine("Save JSON results as: ")
  File(saveAs).writeAll(json)
}
