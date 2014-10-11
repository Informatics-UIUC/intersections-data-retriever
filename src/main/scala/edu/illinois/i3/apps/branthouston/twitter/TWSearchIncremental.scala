package edu.illinois.i3.apps.branthouston.twitter

import java.io.IOException

import com.typesafe.scalalogging.slf4j.Logging
import twitter4j._
import scala.collection.JavaConversions._

import scala.reflect.io.File

object TWSearchIncremental extends App with TwitterAPI with Logging {

  def searchKeyword(keyword: String) = {
    print("+")
    var query = new Query(keyword)
    var tweets = Seq.empty[Status]
    val MAX_RETRIES = 5

    do {
      var attempt = 0
      var result: QueryResult = null
      while (result == null && attempt < MAX_RETRIES) {
        try {
          attempt += 1
          result = twitter.search(query)
          print(".")
        } catch {
          case e @ (_: TwitterException | _: IOException) if attempt < MAX_RETRIES =>
            print("E")
            logger.error(s"Attempt $attempt: ${e.getMessage}")
            Thread.sleep(500 * attempt)
        }
      }

      tweets ++= result.getTweets
      query = result.nextQuery()

      if (result.getRateLimitStatus.getRemaining < 1) {
        val sleepFor = result.getRateLimitStatus.getSecondsUntilReset
        logger.warn("Rate limit reached - sleeping for {} sec", sleepFor.toString)
        print("S")
        //Thread.sleep((sleepFor + 30) * 1000)
        Thread.sleep(TWITTER_RATE_LIMIT_WINDOW_SEC * 1000)

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
