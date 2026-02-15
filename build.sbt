import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.webmvc"
ThisBuild / version := "0.14.1-SNAPSHOT"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/beangle/webmvc"),
    "scm:git@github.com:beangle/webmvc.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = url("http://github.com/duantihua")
  )
)

ThisBuild / description := "The Beangle WebMVC Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/webmvc/index.html"))

val beangle_commons = "org.beangle.commons" % "beangle-commons" % "6.0.6"
val beangle_web = "org.beangle.web" % "beangle-web" % "0.7.4"
val beangle_template = "org.beangle.template" % "beangle-template" % "0.2.5"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-webmvc",
    common,
    libraryDependencies ++= Seq(beangle_commons, beangle_web, beangle_template),
    libraryDependencies ++= Seq(slf4j % "test", logback_classic % "test", scalatest, mockito),
    libraryDependencies ++= Seq(freemarker % "optional")
  )
