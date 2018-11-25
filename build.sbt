name := "JPaxosMonitor"

scalaVersion := "2.12.7"

lazy val root = (project in file(".")).aggregate(JPaxos, common, Replica, PuppetMaster, Client)
  .settings(
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

lazy val JPaxos = ProjectRef(file("../JPaxos"), "jpaxos")

lazy val common = (project in file("common"))
  .settings(
    name := "common",
    libraryDependencies ++= dependencies,
    scalaSource in Compile := baseDirectory.value / "src"
  )

lazy val Replica = (project in file("Replica"))
  .settings(
    name := "Replica",
    libraryDependencies ++= dependencies,
    mainClass in assembly := Some("ReplicaManager"),
    assemblyJarName in assembly := "ReplicaManager.jar",
    scalaSource in Compile := baseDirectory.value / "src"
  ).dependsOn(JPaxos, common)

lazy val PuppetMaster = (project in file("PuppetMaster"))
  .settings(
    name := "PuppetMaster",
    libraryDependencies ++= dependencies,
    mainClass in assembly := Some("PuppetMaster"),
    assemblyJarName in assembly := "PuppetMaster.jar",
    scalaSource in Compile := baseDirectory.value / "src"
  ).dependsOn(JPaxos, common)

lazy val Client = (project in file("Client"))
  .settings(
    name := "Client",
    libraryDependencies ++= dependencies,
    mainClass in assembly := Some("SpammerClient"),
    assemblyJarName in assembly := "Spammer.jar",
    scalaSource in Compile := baseDirectory.value / "src"
  ).dependsOn(JPaxos, common)


val dependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
  "io.spray" %% "spray-json" % "1.3.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
