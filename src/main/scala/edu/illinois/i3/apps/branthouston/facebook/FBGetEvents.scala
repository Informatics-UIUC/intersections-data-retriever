package edu.illinois.i3.apps.branthouston.facebook

import com.typesafe.scalalogging.slf4j.Logging
import facebook4j.Event
import net.liftweb.json._
import org.rogach.scallop.ScallopConf

import scala.collection.JavaConversions._
import scala.io.Source
import scala.reflect.io.File

object FBGetEvents extends App with FacebookAPI with Logging {

  val MAX_RETRIES = 5

  def getEventsForId(id: String) = {
    var attempt = 0
    var events: Option[Seq[Event]] = None
    do {
      try {
        attempt += 1
        events = Some(facebook.getEvents(id))
      } catch {
        case e: Exception =>
          Thread.sleep(500 * attempt)
          logger.error(s"Attempt $attempt: ${e.getMessage}")
      }
    } while (events.isEmpty && attempt < MAX_RETRIES)

    events
  }

  val placesJson = parse(Source.fromFile("fb_places.json").getLines().mkString)

  val sbEvents = new StringBuilder()
  sbEvents.append("id\tstart\tend\tdescription\tname\tlocation\n")

  for {
    JObject(placeJson) <- placesJson
    JField("id", JString(id)) <- placeJson
    JField("name", JString(name)) <- placeJson
  } {
    print(s"Getting events for $name...")
    val events = getEventsForId(id)
    if (events.isDefined) {
      println(s"found ${events.get.size} events")
      // Get page events
      for {
        events <- getEventsForId(id)
        event <- events
      } {
        sbEvents
          .append(event.getId).append("\t")
          .append(event.getStartTime).append("\t")
          .append(event.getEndTime).append("\t")
          .append(event.getDescription).append("\t")
          .append(event.getName).append("\t")
          .append(event.getLocation).append("\n")
      }
    } else
      println("error!")
  }

  File("fb_events.txt").writeAll(sbEvents.toString)
}
