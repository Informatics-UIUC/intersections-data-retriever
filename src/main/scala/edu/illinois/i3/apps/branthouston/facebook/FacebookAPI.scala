package edu.illinois.i3.apps.branthouston.facebook

import facebook4j.{FacebookResponse, FacebookFactory}
import scala.util.{Success, Try}

trait FacebookAPI {
  val facebook = new FacebookFactory().getInstance

  @annotation.tailrec
  final def retry[T <: FacebookResponse](n: Int)(fn: => T): Try[T] = {
    Try { fn } match {
      case x: Success[T] => x
      case _ if n > 1 => retry(n - 1)(fn)
      case f => f
    }
  }
}
