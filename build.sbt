import Dependencies._
import BuildSettings._
import sbt.url

ThisBuild / organization := "org.beangle.webmvc"
ThisBuild / version := "0.4.4-SNAPSHOT"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/webmvc"),
    "scm:git@github.com:beangle/webmvc.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "chaostone",
    name  = "Tihua Duan",
    email = "duantihua@gmail.com",
    url   = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle Data Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/webmvc/index.html"))
ThisBuild / resolvers += Resolver.mavenLocal

lazy val root = (project in file("."))
  .settings()
  .aggregate(core,freemarker,support,bootstrap,showcase)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-webmvc-core",
    commonSettings,
    libraryDependencies ++= (commonDeps ++ Seq(commonsText,javassist,cacheApi,cdiApi,templateApi,scalaxml))
  )

lazy val freemarker = (project in file("freemarker"))
  .settings(
    name := "beangle-webmvc-freemarker",
    commonSettings,
    libraryDependencies ++= (commonDeps ++ Seq(templateFreemarker))
  ).dependsOn(core)

lazy val support = (project in file("support"))
  .settings(
    name := "beangle-webmvc-support",
    commonSettings,
    libraryDependencies ++= (commonDeps ++ Seq(cdiSpring,dataTransfer,dataHibernate,commonsText) ++ itext)
  ).dependsOn(core)

lazy val bootstrap = (project in file("bootstrap"))
  .settings(
    name := "beangle-webmvc-bootstrap",
    commonSettings,
    libraryDependencies ++= commonDeps
  )

lazy val showcase = (project in file("showcase"))
  .settings(
    name := "beangle-webmvc-showcase",
    commonSettings,
    libraryDependencies ++= (commonDeps ++  Seq(templateFreemarker) )
  ).dependsOn(core,support,freemarker,bootstrap)

publish / skip := true
