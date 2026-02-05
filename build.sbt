import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.webmvc"
ThisBuild / version := "0.13.2"

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

val beangle_commons = "org.beangle.commons" % "beangle-commons" % "6.0.0"
val beangle_web = "org.beangle.web" % "beangle-web" % "0.7.3"
val beangle_model = "org.beangle.data" % "beangle-model" % "5.11.7"
val beangle_cdi = "org.beangle.cdi" % "beangle-cdi" % "0.10.0"
val beangle_template = "org.beangle.template" % "beangle-template" % "0.2.4"
val beangle_transfer = "org.beangle.transfer" % "beangle-transfer" % "0.0.4"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-webmvc",
    common,
    libraryDependencies ++= Seq(slf4j, logback_classic, jul_to_slf4j, scalatest, mockito),
    libraryDependencies ++= Seq(beangle_commons, beangle_web),
    libraryDependencies ++= Seq(beangle_template % "optional", beangle_cdi % "optional"),
    libraryDependencies ++= Seq(freemarker % "optional", hibernate_core % "optional"),
    libraryDependencies ++= Seq(beangle_model % "optional", beangle_transfer % "optional")
  )
