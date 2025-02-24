ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.13"

lazy val root = (project in file("."))
  .settings(
    name := "untitled"
  )

val akkaVersion = "2.6.18"
val scalaTestVersion = "3.2.9"
val logbackVersion = "1.2.10"
lazy val cassandraVersion = "0.91"
lazy val leveldbVersion = "0.7"
lazy val leveldbjniVersion = "1.8"
lazy val postgresVersion = "42.2.2"
lazy val protobufVersion = "3.6.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.akka"          %% "akka-persistence" % akkaVersion,

//  "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraVersion,
//  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraVersion % Test,

  // local levelDB stores
  "org.iq80.leveldb"            % "leveldb"          % leveldbVersion,
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % leveldbjniVersion,

  // JDBC with PostgreSQL
//  "org.postgresql" % "postgresql" % postgresVersion,
//  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",

  // Google Protocol Buffers
  "com.google.protobuf" % "protobuf-java"  % protobufVersion,

)
