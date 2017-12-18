import sbt._

object Dependencies {
  lazy val kafkaStreams = Seq(
    "org.apache.kafka" % "kafka-streams" % Versions.kafka,
    "org.apache.kafka" % "kafka-clients" % Versions.kafka
  )

  lazy val sparkjava = Seq(
    "com.sparkjava" % "spark-core" % "2.7.1",
    "com.google.code.gson" % "gson" % "2.8.0"
  )

  lazy val scalaTest = Seq("org.scalatest" %% "scalatest" % "3.0.3" % "test")

  /*
  val kafkaAvroSerializer: Seq[ModuleID] = Seq(
    "io.confluent" % "kafka-avro-serializer" % Versions.confluent
  ).map(_.excludeAll(log4JExclusions: _*))

  val kafkaSchemaRegistry: Seq[ModuleID] = Seq(
    "io.confluent"       % "kafka-schema-registry" % "4.0.0" % "test",
    "io.confluent"       % "kafka-schema-registry" % "4.0.0" % "test" classifier "tests",
    "org.apache.curator" % "curator-test"          % "4.0.0" % "test"
  ).map(_.excludeAll(log4JExclusions ++ jacksonExclusions: _*))

  val kafkaSchemaRegistryClient: Seq[ModuleID] = Seq(
    "io.confluent" % "kafka-schema-registry-client" % Versions.confluent
  ).map(_.excludeAll(log4JExclusions ++ jacksonExclusions: _*))

  val kafka: Seq[ModuleID] = Seq(
    "org.apache.kafka" %% "kafka"        % Versions.kafka,
    "org.apache.kafka" %% "kafka"        % Versions.kafka % "test" classifier "test",
    "org.apache.kafka" % "kafka-clients" % Versions.kafka % "test" classifier "test"
  ).map(_.excludeAll(log4JExclusions: _*))

  val kafkaClient: Seq[ModuleID] = kafka ++ Seq(
    "net.cakesolutions" %% "scala-kafka-client"         % Versions.scalaKafkaClient,
    "net.cakesolutions" %% "scala-kafka-client-testkit" % Versions.scalaKafkaClient % "test"
  ).map(_.excludeAll(log4JExclusions: _*))

  val kafkaStreams: Seq[ModuleID] = kafka ++ Seq(
    "org.apache.kafka" % "kafka-streams"           % Versions.kafka,
    "org.apache.kafka" % "kafka-streams"           % Versions.kafka % "test" classifier "test",
    "fr.psug.kafka"    %% "typesafe-kafka-streams" % Versions.typesafeKafkaStreams
  ).map(_.excludeAll(log4JExclusions: _*))
   */
}

object Versions {
  val confluent = "4.0.0"
  val kafka = "1.0.0"
  val scalaKafkaClient = "1.0.0"
}