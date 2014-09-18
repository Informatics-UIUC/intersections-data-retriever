package edu.illinois.i3.apps.branthouston.twitterclient

import com.typesafe.scalalogging.slf4j.Logging
import scala.collection.JavaConversions._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import scala.reflect.io.File
import scala.language.reflectiveCalls

object FBSearchPage extends App with FacebookAPI with Logging {
  val keyword = readLine("Search pages for: ")

  print("Searching pages...")
  val pages = facebook.searchPages(keyword)
  println(s"${pages.size()} found")

  var allData = pages.map(p => new { val id = p.getId; val name = p.getName })

  val dataJson = pretty(render(allData.map(d =>
    ("id" -> d.id) ~
    ("name" -> d.name)
  )))

  val saveAs = readLine("Save JSON results as: ")
  File(saveAs).writeAll(dataJson)
}
