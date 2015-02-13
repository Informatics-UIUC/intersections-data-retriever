package edu.illinois.i3.apps.branthouston.facebook

import com.typesafe.scalalogging.LazyLogging
import net.liftweb.json.JsonDSL._
import net.liftweb.json._

import scala.collection.JavaConversions._
import scala.language.reflectiveCalls
import scala.reflect.io.File

object FBSearchPage extends App with FacebookAPI with LazyLogging {
  val keyword = io.StdIn.readLine("Search pages for: ")

  print("Searching pages...")
  val pages = facebook.searchPages(keyword)
  println(s"${pages.size()} found")

  var allData = pages.map(p => new { val id = p.getId; val name = p.getName })

  val dataJson = pretty(render(allData.map(d =>
    ("id" -> d.id) ~
    ("name" -> d.name)
  )))

  val saveAs = io.StdIn.readLine("Save JSON results as: ")
  File(saveAs).writeAll(dataJson)
}
