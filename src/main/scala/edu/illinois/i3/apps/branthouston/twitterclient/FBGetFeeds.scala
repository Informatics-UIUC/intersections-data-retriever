package edu.illinois.i3.apps.branthouston.twitterclient

import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import facebook4j.{FacebookFactory, Post}
import net.liftweb.json._

import scala.io.Source
import scala.reflect.io.File

object FBGetFeeds extends App with Logging {

  val facebook = new FacebookFactory().getInstance
  val MAX_RETRIES = 5

  def getFeedForId(id: String) = {
    var attempt = 0
    var feed: Option[Seq[Post]] = None
    do {
      try {
        attempt += 1
        feed = Some(facebook.getFeed(id).filter(_.getMessage != null))
      } catch {
        case e: Exception =>
          Thread.sleep(500*attempt)
          logger.error(s"Attempt $attempt: ${e.getMessage}")
      }
    } while (feed.isEmpty && attempt < MAX_RETRIES)

    feed
  }

  val placesJson = parse(Source.fromFile("fb_places.json").getLines().mkString)

  val sbPosts = new StringBuffer()
  sbPosts.append("msg_id\tpage_id\tmessage\n")

  for {
    JObject(placeJson) <- placesJson
    JField("id", JString(id)) <- placeJson
    JField("name", JString(name)) <- placeJson
  } {
    logger.info(s"Getting posts and comments for $name...")
    // Get feed and comments
    for {
      feed <- getFeedForId(id)
      post <- feed
    } {
      val msg = post.getMessage.replaceAll("\\n", " ")
      sbPosts
        .append(post.getId).append("\t")
        .append(id).append("\t")
        .append(msg).append("\n")

      // Get post comments
      for (comment <- post.getComments) {
        val msg = comment.getMessage
        sbPosts
          .append(comment.getId).append("\t")
          .append(id).append("\t")
          .append(msg).append("\n")
      }
    }
  }

  File("fb_posts.txt").writeAll(sbPosts.toString)
}
