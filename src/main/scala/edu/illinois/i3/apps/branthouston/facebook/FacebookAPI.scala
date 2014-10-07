package edu.illinois.i3.apps.branthouston.facebook

import com.typesafe.scalalogging.slf4j.Logging
import facebook4j.FacebookFactory
import scala.util.{Failure, Success, Try}

trait FacebookAPI extends Logging {
  val facebook = new FacebookFactory().getInstance

  @annotation.tailrec
  final def retry[T](n: Int)(fn: => T): Try[T] = {
    Try { fn } match {
      case x: Success[T] => x
      case Failure(e) if n > 1 =>
        logger.warn("Error invoking the Facebook API", e)
        retry(n - 1)(fn)
      case f => f
    }
  }
}
