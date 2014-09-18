package edu.illinois.i3.apps.branthouston.twitterclient

import twitter4j.{TwitterResponse, TwitterFactory}

import scala.util.{Success, Try}

trait TwitterAPI {
  val TWITTER_RATE_LIMIT_WINDOW_SEC = 15*60
  
  val twitter = TwitterFactory.getSingleton

  @annotation.tailrec
  final def retry[T <: TwitterResponse](n: Int)(fn: => T): Try[T] = {
    Try {
      fn

      // TODO: check rate limit on PREVIOUS result (add as parameter to method call)
    } match {
      case x: Success[T] => x
      case _ if n > 1 => retry(n - 1)(fn)
      case f => f
    }
  }
}
