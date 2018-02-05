organization:="organization name"
name := "boson"
version := "version"

scalaVersion := "2.12.3"

javacOptions += "-g:none"

scalacOptions in Test ++= Seq(
  "-encoding",
  "UTF-8"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v")

compileOrder := CompileOrder.Mixed
compileOrder in Test:= CompileOrder.Mixed

//  Creates a jar with all libraries necessary
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

  val libraries = Seq(
    "javax.json" % "javax.json-api" % "1.1",
    "org.glassfish" % "javax.json" % "1.1",
    "de.undercouch" % "bson4jackson" % "2.7.0",
    "io.vertx" % "vertx-core" % "3.5.0",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
    "com.storm-enroute" % "scalameter-core_2.12" % "0.8.2",
    "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
    "io.rest-assured" % "scala-support" % "3.0.6",
    "io.rest-assured" % "rest-assured" % "3.0.6",
    "com.squareup.okhttp3" % "mockwebserver" % "3.9.1"
  )
  val testLibraries = Seq(
    "org.scalatest"     %% "scalatest"   % "3.0.3" % Test withSources(),
    "junit"             %  "junit"       % "4.12"  % Test,
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "com.jayway.jsonpath" % "json-path" % "2.4.0",
    "org.slf4j" % "slf4j-simple" % "1.7.25"
  )


libraryDependencies ++= libraries ++ testLibraries
homepage := Some(url(s"https://www.example.com"))
description := "A Maven Central example"
licenses += "The Apache License, Version 2.0" ->
  url("http://www.apache.org/licenses/LICENSE-2.0.txt")
// The developers of the project
pomIncludeRepository := { _ => false }
publishArtifact in Test := false
developers := List(
  Developer(
    id="developerID",
    name="developerName",
    email="developerEmail",
    url=url("https://www.example.com/Ricardo/")
  )
)
// Information about the source code repository of your code
scmInfo := Some(
  ScmInfo(
    url("https://github.com/<github username>/<project name>"),
    "scm:git@github.com:<github username>/<project name>.git"
  )
)

useGpg := true
publishMavenStyle := true
//publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  val v = version.value
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  "jira account username",
  "jira account password")