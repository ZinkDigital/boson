name := "boson"
organization:="com.github.ricardoffmartins"
version := "1.2-SNAPSHOT"

scalaVersion := "2.12.3"

javacOptions += "-g:none"
compileOrder := CompileOrder.Mixed



  val libraries = Seq(
    "javax.json" % "javax.json-api" % "1.1",
    "org.glassfish" % "javax.json" % "1.1",
    "de.undercouch" % "bson4jackson" % "2.7.0",
    "io.vertx" % "vertx-core" % "3.5.0",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

  )
  val testLibraries = Seq(
    "org.scalatest"     %% "scalatest"   % "3.0.3" % Test withSources(),
    "junit"             %  "junit"       % "4.12"  % Test
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
    id="ricardoffmartins",
    name="Ricardo Martins",
    email="rffmartins64@hotmail.com",
    url=url("https://www.example.com/Ricardo/")
  )
)
// Information about the source code repository of your code
scmInfo := Some(
  ScmInfo(
    url("https://github.com/ricardoffmartins/boson"),
    "scm:git@github.com:ricardoffmartins/boson.git"
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
  "Ricardoffmartins",
  "RFFM_64053Growin")

