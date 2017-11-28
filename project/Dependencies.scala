import sbt._

object Dependencies {
  lazy val kafkaStreams = Seq(
    "org.apache.kafka" % "kafka-streams" % "0.11.0.0",
    "org.apache.kafka" % "kafka-clients" % "0.11.0.0"
  )

  lazy val sparkjava = Seq(
    "com.sparkjava" % "spark-core" % "2.7.1",
    "com.google.code.gson" % "gson" % "2.8.0"
  )

  lazy val scalaTest = Seq("org.scalatest" %% "scalatest" % "3.0.3" % "test")
}
