name := "MercatorTest"

version := "0.1"

scalaVersion := "2.12.13"

libraryDependencies += "org.scalactic" % "scalactic_2.12" % "3.2.5"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.2.5"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.4" % Test

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
// Presence of this line causes exception at start time
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.3.0-alpha12"
libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "2.4.3"
libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "2.4.3"
