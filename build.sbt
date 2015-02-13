name := "intersections"

version := "0.8-SNAPSHOT"

organization := "edu.illinois.i3.projects.branthouston"

scalaVersion := "2.11.5"

scalacOptions := Seq("-feature")

resolvers ++= Seq(
  "I3 Repository" at "http://htrc.illinois.edu/nexus/content/groups/public"
)

libraryDependencies ++= Seq(
  "org.rogach"                    %% "scallop"            % "0.9.5",
  "com.typesafe.scala-logging"    %% "scala-logging"      % "3.1.0",
  "ch.qos.logback"                %  "logback-classic"    % "1.1.2",
  "org.twitter4j"                 %  "twitter4j-core"     % "4.0.2",
  "org.facebook4j"                %  "facebook4j-core"    % "2.2.2",
  "net.liftweb"                   %% "lift-json"          % "2.6-RC1",
  "com.github.nscala-time"        %% "nscala-time"        % "1.8.0",
  "org.scalatest"                 %% "scalatest"          % "2.2.1"        % "test"
)
