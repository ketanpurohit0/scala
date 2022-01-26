lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.13.6"
    )),
    name := "fps_manning"
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % Test
