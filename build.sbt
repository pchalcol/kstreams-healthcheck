import Dependencies._

resolvers += "Confluent" at "http://packages.confluent.io/maven/"

resolvers += Resolver.mavenLocal

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.8",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "kstreams-healthcheck",
    libraryDependencies ++= scalaTest ++
      kafkaStreams ++
      sparkjava
  )
