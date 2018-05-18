import Dependencies._
import sbt.Resolver

val basicSettings = Seq(

organization:="io.zink",

version := "0.5.0",

scalaVersion := "2.12.3",

 javacOptions  ++= Seq("-Xdoclint:none","-g:none"),

//javacOptions += "-g:none",
scalacOptions in Test ++= Seq(
  "-encoding",
  "UTF-8"
),

testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),

compileOrder := CompileOrder.Mixed,
compileOrder in Test:= CompileOrder.Mixed,

//  Creates a jar with all libraries necessary
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
},

libraryDependencies ++= libraries ++ testLibraries,
homepage := Some( url("https://github.com/ZinkDigital/boson")),
description := "Boson - Streaming for JSON and BSON",
licenses += "The Apache License, Version 2.0" ->
  url("http://www.apache.org/licenses/LICENSE-2.0.txt"),

pomIncludeRepository := { _ => false },
publishArtifact in Test := false,


// The developers of the project
developers := List(
  Developer(
    id="Zink Digital",
    name= "zink",
    email="hello@zink.io",
    url=url("http://www.zink.io")
  )
  // then Growin' in here
),
// Information about the source code repository of your code
scmInfo := Some(
  ScmInfo(
    url("https://github.com/ZinkDigital/boson"),
    "scm:git@github.com:ZinkDigital/boson.git"
    )
  ),
  //useGpg := true,
  //pgpReadOnly := true,
                                        
credentials += Credentials(Path.userHome /".sbt" /".credentials"),
 
  //pgpPassphrase := Some("boson0000".toArray),
  pgpSecretRing := file("/Users/ricardomartins/.gnupg/secring.gpg"),
  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    val v = version.value
    if (v.trim.endsWith("SNAPSHOT"))
      //Opts.resolver.sonatypeSnapshots
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      //Opts.resolver.sonatypeStaging
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  }
)
val noPublishing = Seq(
  publishArtifact := false,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))))

 

def javaDoc = Seq(
  doc in Compile := {
    val cp = (fullClasspath in Compile in doc).value
    val docTarget = (target in Compile in doc).value
    val compileSrc = (javaSource in Compile).value
    val s = streams.value
    //def replace(x: Any) = x.toString.replace("boson-java", "boson-core")
    def docLink = name.value match {
      case "boson-java" => ""
      case _ => ""
    }
    val cmd = "javadoc" +
      " -sourcepath " + compileSrc +
      " -classpath " + cp.map(_.data).mkString(":") +
      " -d " + docTarget +
      docLink +
      " -encoding utf8" +
      " -public" +
      " -windowtitle " + name.value + "_" + version.value +
      " -subpackages" +
      " io.zink"
    s.log.info(cmd)
    sys.process.Process(cmd)
    docTarget
  }
)

val libraries = Seq(
  "org.glassfish" % "javax.json" % "1.1",

  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "io.netty" % "netty-all" % "4.1.22.Final",

  "org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0",
  "com.chuusai" % "shapeless_2.12" % "2.3.3",
  "org.scala-lang" % "scala-compiler" % "2.11.12",
  "org.parboiled" %% "parboiled" % "2.1.4"//,
//
//  "com.typesafe.akka" %% "akka-http" % "10.0.5",
// "com.typesafe.akka" %% "akka-actor" % "2.5.1",
// "com.typesafe.akka" %% "akka-stream" % "2.5.1",
// "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.1",
//"com.typesafe.akka" %% "akka-testkit" % "2.5.1",
//"com.typesafe.akka" %% "akka-http-testkit" % "10.0.5"

)

val testLibraries = Seq(
  "org.scalatest"     %% "scalatest"   % "3.0.3" % Test withSources(),
  "de.undercouch" % "bson4jackson" % "2.7.0",
  "junit"             %  "junit"       % "4.12"  % Test,
  "io.vertx" % "vertx-core" % "3.5.0",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "com.jayway.jsonpath" % "json-path" % "2.4.0",
  "com.google.code.gson" % "gson" % "2.3.1",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "io.rest-assured" % "scala-support" % "3.0.6",
  "io.rest-assured" % "rest-assured" % "3.0.6",
  "com.squareup.okhttp3" % "mockwebserver" % "3.9.1",
  "com.storm-enroute" %% "scalameter" % "0.8.2"
)




lazy val root = project.in( file("."))
    .aggregate(bosonCore, bosonScala, bosonJava)
  .settings(basicSettings: _*)
  .settings (noPublishing: _*)

lazy val bosonCore = project.in(file("boson-core"))
  .settings(basicSettings: _*)
  .settings(javaDoc:  _*)
  .settings(
    libraryDependencies ++= Dependencies.compile(asm, asmTree, asmAnalysis, asmUtil),
    javacOptions in Test += "-g", // needed for bytecode rewriting
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val bosonScala = project.in(file("boson-scala"))
    .dependsOn(bosonCore)
  .settings(basicSettings: _*)
  .settings(javaDoc:  _*)
  .settings(
    libraryDependencies ++= Dependencies.compile(asm, asmTree, asmAnalysis, asmUtil),
    javacOptions in Test += "-g", // needed for bytecode rewriting
    crossPaths := false,
    autoScalaLibrary := false
  )

lazy val bosonJava = project.in(file("boson-java"))
  .dependsOn(bosonCore)
  .settings(basicSettings: _*)
  .settings(javaDoc:  _*)
  .settings(
    libraryDependencies ++= Dependencies.compile(asm, asmTree, asmAnalysis, asmUtil),
    javacOptions in Test += "-g", // needed for bytecode rewriting
    crossPaths := false,
    autoScalaLibrary := false
  )

