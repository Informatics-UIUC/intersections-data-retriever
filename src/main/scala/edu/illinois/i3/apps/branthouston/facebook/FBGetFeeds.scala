package edu.illinois.i3.apps.branthouston.facebook

import com.typesafe.scalalogging.slf4j.Logging
import facebook4j.Post
import net.liftweb.json._

import scala.collection.JavaConversions._
import scala.io.Source
import scala.reflect.io.File

object FBGetFeeds extends App with FacebookAPI with Logging {

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

  val input = readLine("Load: ")
  val pagesJson = parse(Source.fromFile(input).getLines().mkString)

  val sbPosts = new StringBuilder()
  sbPosts.append("msg_id\tpage_id\tmessage\n")

  for {
    JObject(pageJson) <- pagesJson
    JField("id", JString(id)) <- pageJson
    JField("name", JString(name)) <- pageJson
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
        val msg = comment.getMessage.replaceAll("\\n", " ")
        sbPosts
          .append(comment.getId).append("\t")
          .append(id).append("\t")
          .append(msg).append("\n")
      }
    }
  }

  val saveAs = readLine("Save as: ")
  File(saveAs).writeAll(sbPosts.toString())
}
