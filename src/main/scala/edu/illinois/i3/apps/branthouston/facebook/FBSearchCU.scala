package edu.illinois.i3.apps.branthouston.facebook

import com.typesafe.scalalogging.slf4j.Logging
import facebook4j.{GeoLocation, Post}

import scala.collection.JavaConversions._
import scala.reflect.io.File

object FBSearchCU extends App with FacebookAPI with Logging {
  val ChampaignCounty = new GeoLocation(40.140300, -88.196100)
  val MAX_RETRIES = 5

  // facebook data limits:
  //    wall post from the last 30 days or 50 posts, whichever is fewer

  def fbSearchPlace(placeName: String, geo: GeoLocation, distance: Int, skipIds: Set[String] = Set.empty[String]) = {
    facebook.searchPlaces(placeName, geo, distance).collect {
      case place if !skipIds.contains(place.getId) =>
        val id = place.getId
        logger.info(s"Getting feed for page ${place.getName} (id: $id)")
        var attempt = 0
        var feed: Option[Seq[Post]] = None
        do {
          try {
            attempt += 1
            feed = Some(facebook.getFeed(id).filter(_.getMessage != null))
          } catch {
            case e: Exception =>
              logger.error(s"Attempt $attempt: ${e.getMessage}")
              Thread.sleep(500*attempt)
          }
        } while (feed.isEmpty && attempt < MAX_RETRIES)
        place -> feed
    }.collect {
      case (place, feed) if feed.isDefined => (place, feed.get)
    }
  }

  var skipPageIds = Set.empty[String]
  val champaignFeeds = fbSearchPlace("champaign", ChampaignCounty, 32000, skipPageIds)
  skipPageIds ++= champaignFeeds.map(_._1.getId)
  val urbanaFeeds = fbSearchPlace("urbana", ChampaignCounty, 32000, skipPageIds)
  skipPageIds ++= urbanaFeeds.map(_._1.getId)
  val chambanaFeeds = fbSearchPlace("chambana", ChampaignCounty, 32000, skipPageIds)

  val allFeeds = champaignFeeds ++ urbanaFeeds ++ chambanaFeeds

  val sbFB = new StringBuffer()
  for {
    (place, posts) <- allFeeds
    post <- posts
  } {
    val msg = post.getMessage.replaceAll("\\n", " ")
    sbFB.append(post.getId).append("\t").append(place.getId).append("\t").append(msg).append("\n")
  }

  File("fb_CU_pages.txt").writeAll(sbFB.toString)
}
