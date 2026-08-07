import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

organization := "org.beangle.webmvc"
version := "0.15.1"

scmInfo := Some(
  ScmInfo(
    uri("https://github.com/beangle/webmvc"),
    "scm:git@github.com:beangle/webmvc.git"
  )
)

developers := List(
  Developer(
    id = "chaostone",
    name = "Tihua Duan",
    email = "duantihua@gmail.com",
    url = uri("http://github.com/duantihua")
  )
)

description := "The Beangle WebMVC Library"
homepage := Some(uri("https://beangle.github.io/webmvc/index.html"))

val beangle_commons = "org.beangle.commons" % "beangle-commons" % "6.2.1"
val beangle_web = "org.beangle.web" % "beangle-web" % "0.7.8"
val beangle_template = "org.beangle.template" % "beangle-template" % "0.2.8"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-webmvc",
    common,
    libraryDependencies ++= Seq(beangle_commons, beangle_web, beangle_template),
    libraryDependencies ++= Seq(slf4j % "test", logback_classic % "test", scalatest, mockito),
    libraryDependencies ++= Seq(freemarker % "optional")
  )
