name := "JPaxosMonitor"

scalaVersion := "2.12.7"

lazy val JPaxos = ProjectRef(file("../JPaxos"), "jpaxos")

lazy val Replica = (project in file ("Replica"))
	.settings(
		name := "Replica",
		libraryDependencies ++= dependencies,
		mainClass in assembly := Some("ReplicaManager"),
		assemblyJarName in assembly := "ReplicaManager.jar",
		scalaSource in Compile := baseDirectory.value / "src"
	).dependsOn(JPaxos)

lazy val PuppetMaster = (project in file ("PuppetMaster"))
	.settings(
		name := "PuppetMaster",
		libraryDependencies ++= dependencies,
		mainClass in assembly := Some("PuppetMaster"),
		assemblyJarName in assembly := "PuppetMaster.jar",
		scalaSource in Compile := baseDirectory.value / "src"
	).dependsOn(JPaxos)

lazy val Client = (project in file ("Client"))
	.settings(
		name := "Client",
		libraryDependencies ++= dependencies,
		mainClass in assembly := Some("Client"),
		assemblyJarName in assembly := "Client.jar",
		scalaSource in Compile := baseDirectory.value / "src"
	).dependsOn(JPaxos)


val dependencies = Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12"
)

/*
libraryDependencies ++= Seq(
scalaSource in Test := baseDirectory.value / "test"
  "com.typesafe.akka" %% "akka-http"   % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12"
)
*/

//javaOptions += "-DLogback.configurationFile=./lib/logback.xml"

