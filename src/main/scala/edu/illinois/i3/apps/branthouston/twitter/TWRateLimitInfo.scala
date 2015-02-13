package edu.illinois.i3.apps.branthouston.twitter

import com.typesafe.scalalogging.LazyLogging

object TWRateLimitInfo extends App with TwitterAPI with LazyLogging {

  val rateLimitStatus = twitter.getAPIConfiguration.getRateLimitStatus

  logger.info("Limit: {}", rateLimitStatus.getLimit.toString)
  logger.info("Remaining: {}", rateLimitStatus.getRemaining.toString)
  logger.info("Reset time: {}", rateLimitStatus.getResetTimeInSeconds.toString)
  logger.info("Time until reset: {} sec", rateLimitStatus.getSecondsUntilReset.toString)
}
