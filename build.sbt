name := """hello-akka"""

version := "1.0"

scalaVersion := "2.11.7"

lazy val akkaVersion = "2.4.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "org.apache.spark" %% "spark-core" % "1.5.2",
  "org.apache.spark"  %% "spark-streaming" % "1.5.2"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
