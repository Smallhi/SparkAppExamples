import _root_.sbtassembly.Plugin.AssemblyKeys
import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

assemblySettings

name := "SparkAppExamples"

version := "0.1"

scalaVersion := "2.11.8"

// Resolvers
resolvers += "SnowPlow Repo" at "http://maven.snplow.com/releases/"
resolvers += "Twitter Maven Repo" at "http://maven.twttr.com/"
resolvers += "MMLSpark Repo" at "https://mmlspark.azureedge.net/maven"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.0.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.0.0" % "provided",
  "org.apache.spark" %% "spark-hive" % "2.0.0" % "provided",
  "org.apache.spark" %% "spark-streaming" % "2.0.0" % "provided",
  "org.apache.spark" %% "spark-graphx" % "2.0.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.0.0" % "provided",
   "org.jsoup" % "jsoup" % "1.11.1",
  // "com.microsoft.ml.spark" %% "mmlspark" % "0.10",
//  ,
  "com.snowplowanalytics"  %% "scala-maxmind-iplookups"  % "0.2.0"
)

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case PathList(ps@_*) if ps.last endsWith "eclipse.inf" => MergeStrategy.last
  case PathList(ps@_*) if ps.last endsWith "pom.xml" => MergeStrategy.last
  case PathList(ps@_*) if ps.last endsWith "pom.properties" => MergeStrategy.last
  case PathList("com", "sun", xs@_*) => MergeStrategy.last
  case PathList("javax", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", xs@_*) => MergeStrategy.last
  case PathList("org", "aopalliance", xs@_*) => MergeStrategy.last
  case PathList("org", "w3c", xs@_*) => MergeStrategy.last
  case "overview.html" => MergeStrategy.last
  case x => old(x)
}
}