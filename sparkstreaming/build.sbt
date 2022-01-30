name := "sparkstreaming"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies += "org.scalactic" % "scalactic_2.13" % "3.2.5"
libraryDependencies += "org.scalatest" % "scalatest_2.13" % "3.2.5"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.15.4" % Test

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.1"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.2.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.2.1"
