import sbt._

val akkaVersion     = "2.6.8"
val akkaHttpVersion = "10.2.7"
val jacksonVersion  = "2.10.0"
val monocleVersion  = "2.0.0"
val circeVersion    = "0.12.3"
val swaggerVersion  = "2.0.9"

val spray                  = "io.spray"                   %% "spray-json"               % "1.3.6"
val scalalogging           = "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.2"
val scalaTest              = "org.scalatest"              %% "scalatest"                % "3.0.1" % Test
val monoclecore            = "com.github.julien-truffaut" %% "monocle-core"             % "2.0.0"
val monoclemacro           = "com.github.julien-truffaut" %% "monocle-macro"            % "2.0.0"
val monoclelaw             = "com.github.julien-truffaut" %% "monocle-law"              % "2.0.0" % "test"
val mysql                  = "mysql"                      % "mysql-connector-java"      % "5.1.46"
val slick                  = "com.typesafe.slick"         %% "slick"                    % "3.2.3"
val slickHikariCp          = "com.typesafe.slick"         %% "slick-hikaricp"           % "3.2.3"
val scalaUuid              = "io.jvm.uuid"                %% "scala-uuid"               % "0.3.1"
val rsApi                  = "javax.ws.rs"                % "javax.ws.rs-api"           % "2.0.1"
val akkaHttp               = "com.typesafe.akka"          %% "akka-http"                % akkaHttpVersion
val akkaSpray              = "com.typesafe.akka"          %% "akka-http-spray-json"     % akkaHttpVersion
val akkaActor              = "com.typesafe.akka"          %% "akka-actor"               % akkaVersion
val akkaStream             = "com.typesafe.akka"          %% "akka-stream"              % akkaVersion
val akkaSlf4j              = "com.typesafe.akka"          %% "akka-slf4j"               % akkaVersion
val akkaStreamTestKit      = "com.typesafe.akka"          %% "akka-stream-testkit"      % akkaVersion
val akkaHttpTestKit        = "com.typesafe.akka"          %% "akka-http-testkit"        % akkaHttpVersion
val akkaHttpCors           = "ch.megard"                  %% "akka-http-cors"           % "0.4.1"
val slf4j                  = "org.slf4j"                  % "slf4j-simple"              % "1.7.28"
val slickJodaMapper        = "com.github.tototoshi"       %% "slick-joda-mapper"        % "2.3.0"
val joda                   = "org.joda"                   % "joda-convert"              % "1.7"
val shapeless              = "com.chuusai"                %% "shapeless"                % "2.3.3"
val pprint                 = "com.lihaoyi"                %% "pprint"                   % "0.5.3"
val circe                  = "de.heikoseeberger"          %% "akka-http-circe"          % "1.29.1"
val playJson               = "com.typesafe.play"          %% "play-json"                % "2.7.2"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    inThisBuild(
      List(
        organization := "com.example",
        scalaVersion := "2.12.8",
        version := "0.0.1-SNAPSHOT"
      )
    ),
    name := "dev-test",
    resolvers += "Typesafe Repo" at "https://repo.typesafe.com/typesafe/releases/",
    libraryDependencies ++= Seq(
      rsApi,
      akkaHttp,
      akkaSpray,
      akkaActor,
      akkaStream,
      akkaSlf4j,
      akkaStreamTestKit,
      akkaHttpTestKit,
      akkaHttpCors,
      slf4j,
      slickJodaMapper,
      joda,
      scalaTest,
      spray,
      scalalogging,
      slick,
      monoclecore,
      monoclelaw,
      monoclemacro,
      mysql,
      slick,
      slickHikariCp,
      scalaUuid,
      playJson
    ),
  )
