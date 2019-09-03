name := """play-getting-started"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  filters,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  ws,
  "org.scalaj" %% "scalaj-http" % "2.4.1",
  "com.google.cloud" % "google-cloud-firestore" % "1.7.0"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )
