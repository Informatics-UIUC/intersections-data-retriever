package edu.illinois.i3.apps.branthouston.twitterclient

import facebook4j.FacebookFactory

trait FacebookAPI {
  val facebook = new FacebookFactory().getInstance
}
