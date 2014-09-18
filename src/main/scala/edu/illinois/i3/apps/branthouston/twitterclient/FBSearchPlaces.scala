package edu.illinois.i3.apps.branthouston.twitterclient

import com.typesafe.scalalogging.slf4j.Logging
import facebook4j.{Place, GeoLocation}
import scala.collection.JavaConversions._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.io.Source
import scala.reflect.io.File

object FBSearchPlaces extends App with FacebookAPI with Logging {

  val ChampaignCounty = new GeoLocation(40.140300, -88.196100)

  var allPlaces = Set.empty[Place]
  for (placeKw <- Source.fromFile("places.txt").getLines()) {
    print(s"Searching for '$placeKw'...")
    val places = facebook.searchPlaces(placeKw, ChampaignCounty, 32000)
    println(s"${places.size()} found")
    allPlaces ++= places
  }

  println(s"Total number of places found: ${allPlaces.size}")

  val placesJson = pretty(render(allPlaces.map(p =>
    ("id" -> p.getId) ~
    ("name" -> p.getName) ~
    ("location" ->
      ("text" -> p.getLocation.getText) ~
      ("street" -> p.getLocation.getStreet) ~
      ("city" -> p.getLocation.getCity) ~
      ("state" -> p.getLocation.getState) ~
      ("zip" -> p.getLocation.getZip) ~
      ("country" -> p.getLocation.getCountry) ~
      ("lat" -> JDouble(p.getLocation.getLatitude)) ~
      ("lon" -> JDouble(p.getLocation.getLongitude))
    )
  )))

  File("fb_places.json").writeAll(placesJson)
}
