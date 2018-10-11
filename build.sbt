name := "JPaxosMonitor"

scalaSource in Compile := baseDirectory.value / "src"
scalaSource in Test := baseDirectory.value / "test"

scalaVersion := "2.12.7"

mainClass in assembly := Some("replica.ReplicaManager")
assemblyJarName in assembly := "ReplicaManager.jar"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12"
)

//javaOptions += "-DLogback.configurationFile=./lib/logback.xml"

