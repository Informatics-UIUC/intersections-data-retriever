name := "twitter-client"

version := "0.2-SNAPSHOT"

organization := "edu.illinois.i3.apps.branthouston.twitterclient"

scalaVersion := "2.10.4"

scalacOptions := Seq("-feature")

resolvers ++= Seq(
  "I3 Repository" at "http://htrc.illinois.edu/nexus/content/groups/public"
)

libraryDependencies ++= Seq(
  "com.typesafe"                  %% "scalalogging-slf4j" % "1.0.1",
  "ch.qos.logback"                %  "logback-classic"    % "1.0.13",
  "org.twitter4j"                 %  "twitter4j-core"     % "4.0.2",
  "org.facebook4j"                % "facebook4j-core"     % "2.1.0",
  "net.liftweb"                   %% "lift-json"          % "2.6-RC1",
  "org.scalatest"                 %% "scalatest"          % "2.0"        % "test"
)