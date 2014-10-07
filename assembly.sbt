import AssemblyKeys._

assemblySettings

mainClass in assembly := Some("edu.illinois.i3.apps.branthouston.facebook.FBGetEventsRaw")

jarName in assembly <<= (name, version) map { (name, version) => name + "-" + version + ".jar" }