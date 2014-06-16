import sbt._
import Keys._

object SpraySocketIOTestBuild extends Build {

  object Versions {
    val Scala = "2.10.4"
    val Akka  = "2.3.3"
    val Spray = "1.3.2-20140428"
    val SprayJson = "1.2.6"
    val SpraySocketIO = "0.1.1-SNAPSHOT"
  }

  val buildSettings = Defaults.coreDefaultSettings ++ Seq(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := Versions.Scala,
    resolvers ++= Seq(
      "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
      "spray" at "http://repo.spray.io",
      "spray nightly" at "http://nightlies.spray.io/"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % Versions.Scala,
      "com.wandoulabs.akka" %% "spray-socketio" % Versions.SpraySocketIO,
      "io.spray" % "spray-can" % Versions.Spray,
      "io.spray" % "spray-json_2.10" % Versions.SprayJson,
      "com.typesafe.akka" %% "akka-actor" % Versions.Akka,
      "com.typesafe.akka" %% "akka-contrib" % Versions.Akka
    )
//  val parboiled = "org.parboiled" %% "parboiled-scala" % "1.1.5"
//  val parboiled2 = "org.parboiled" %% "parboiled" % "2.0-M2" //changing ()

//  val akka_multinode_testkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % AKKA_VERSION % "test"
//  val scalatest = "org.scalatest" %% "scalatest" % "2.0" % "test"
//  val rxscala = "com.netflix.rxjava" % "rxjava-scala" % "0.17.1"
//  val apache_math = "org.apache.commons" % "commons-math3" % "3.2" // % "test"
//  val caliper = "com.google.caliper" % "caliper" % "0.5-rc1" % "test"
  )

  val root = Project(
    id = "spray-socket-io-test",
    base = file("."),
    settings = buildSettings)
}
