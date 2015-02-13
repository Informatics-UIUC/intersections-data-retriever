import AssemblyKeys._

assemblySettings

mainClass in assembly := Some("edu.illinois.i3.apps.branthouston.twitter.TWSearchKeyword")

jarName in assembly <<= (name, version) map { (name, version) => "twitterSearch" + "-" + version + ".jar" }