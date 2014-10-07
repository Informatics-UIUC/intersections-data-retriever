package edu.illinois.i3.apps.branthouston.facebook

import java.io.File
import com.typesafe.scalalogging.slf4j.Logging
import facebook4j.json.DataObjectFactory
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import org.joda.time.format.ISODateTimeFormat
import org.rogach.scallop.ScallopConf
import scala.collection.JavaConversions._
import scala.io.Source
import com.github.nscala_time.time.Imports._


object FBGetEventsRaw extends App with FacebookAPI with Logging {

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val (appTitle, appVersion, appVendor) = {
      val p = getClass.getPackage
      val nameOpt = Option(p.getImplementationTitle)
      val versionOpt = Option(p.getImplementationVersion)
      val vendorOpt = Option(p.getImplementationVendor)
      (nameOpt, versionOpt, vendorOpt)
    }

    version(appTitle.flatMap(
      name => appVersion.flatMap(
        version => appVendor.map(
          vendor => s"$name $version\n$vendor"))).getOrElse("FBGetEventsRaw"))

    val placesFile = opt[File]("places",
      descr = "Places file to search",
      required = true
    )

    val outputFile = opt[String]("output",
      descr = "Output file",
      required = true
    )

    val retryCount = opt[Int]("retry",
      descr = "Number of times to retry when calling the Facebook API in case of error",
      default = Some(3)
    )
  }

  // Parse the command line args and extract values
  val conf = new Conf(args)
  val placesFile = conf.placesFile()
  val outputFile = conf.outputFile()
  val retryCount = conf.retryCount()

  val placesJson = parse(Source.fromFile(placesFile).getLines().mkString)
  var allEventsJson = List.empty[String]

  def parseDateTime(dateTimeStr: String, timezone: Option[String] = None) = {
    val tz = timezone.map(DateTimeZone.forID).orElse(Some(DateTimeZone.getDefault())).get
    ISODateTimeFormat.dateOptionalTimeParser().withZone(tz).parseDateTime(dateTimeStr)
  }
  
  def toBsonDate(dateTimeStr: String, jsonTimezone: JValue) = {
    val tz = jsonTimezone match {
      case JString(s) => Some(s)
      case _ => None
    }
    val date = parseDateTime(dateTimeStr, tz)
    JField("$date", "$numberLong" -> date.getMillis)
  }

  for {
    JObject(placeJson) <- placesJson
    JField("id", JString(id)) <- placeJson
    JField("name", JString(name)) <- placeJson
  } {
    var eventCount = 0
    var events = retry(retryCount) { facebook.getEvents(id) }
    while (events.isSuccess && events.get != null && events.get.nonEmpty) {
      eventCount += events.get.size
      allEventsJson ++= events.get
        .map(e => parse(DataObjectFactory.getRawJSON(facebook.getEvent(e.getId))))
        .map(json => json transform {
          case JField("id", sid) => JField("_id", "$numberLong" -> sid)
          case JField("start_time", JString(startTime)) => JField("start_time", toBsonDate(startTime, json \ "timezone"))
          case JField("end_time", JString(endTime)) => JField("end_time", toBsonDate(endTime, json \ "timezone"))
          case JField("updated_time", JString(updatedTime)) => JField("updated_time", toBsonDate(updatedTime, json \ "timezone"))
          case JField("timezone", _) => JNothing
        })
        .map(compactRender(_) + "\n")
      val paging = events.get.getPaging
      events = retry(retryCount) { facebook.fetchNext(paging) }
    }

    logger.debug("Found {} events for {}", eventCount.toString, name)
  }

  logger.info("Found {} total events", allEventsJson.size.toString)
  scala.reflect.io.File(outputFile).writeAll(allEventsJson: _*)
}
