package edu.illinois.i3.apps.branthouston.twitterclient

import java.io.IOException

import com.typesafe.scalalogging.slf4j.Logging
import twitter4j._
import scala.collection.JavaConversions._

import scala.reflect.io.File

object TWSearchKeyword extends App with TwitterAPI with Logging {

  def searchKeyword(keyword: String) = {
    print("+")
    var query = new Query(keyword)
    var tweets = List.empty[Status]
    val MAX_RETRIES = 5

    do {
      var attempt = 0
      var lastError: Throwable = null
      var result: QueryResult = null
      while (result == null && attempt < MAX_RETRIES) {
        try {
          attempt += 1
          result = twitter.search(query)
          print(".")
        } catch {
          case e @ (_: TwitterException | _: IOException) =>
            print("E")
            lastError = e
            logger.error(s"Attempt $attempt: ${e.getMessage}")
            Thread.sleep(500 * attempt)
        }
      }

      if (attempt == MAX_RETRIES)
        throw lastError

      tweets ++= result.getTweets
      query = result.nextQuery()

      if (result.getRateLimitStatus.getRemaining < 1) {
        val sleepFor = result.getRateLimitStatus.getSecondsUntilReset
        logger.warn("Rate limit reached - sleeping for {} sec", sleepFor.toString)
        print("S")
        Thread.sleep((sleepFor + 30) * 1000)
        logger.info("Resuming...")
      }
    } while (query != null)

    tweets
  }

  val keyword = readLine("Keyword: ")
  print("Searching Twitter...")
  var tweets = searchKeyword(keyword).toSet
  val numTweets = tweets.size
  println(s"$numTweets tweets found")

  val users = tweets.map(_.getUser.getScreenName).toSet
  print(s"Searching feeds of ${users.size} users...")

  import TWGetUserTimeline.getUserTimeline
  tweets ++= users.flatMap(getUserTimeline).filter(_.getText.toLowerCase.contains(keyword.toLowerCase))

  val numTweetsDiff = tweets.size - numTweets
  println(s"$numTweetsDiff more tweets found")

  val sbTweets = new StringBuffer()
  sbTweets.append("id\tuser\tmessage\n")

  for (tweet <- tweets) {
    val msg = tweet.getText.replaceAll("\\n", " ")
    sbTweets
      .append(tweet.getId).append("\t")
      .append(tweet.getUser.getScreenName).append("\t")
      .append(msg).append("\n")
  }

  val saveAs = readLine("Save as: ")
  File(saveAs).writeAll(sbTweets.toString)
}
