import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.webmvc"
ThisBuild / version := "0.10.6"

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

val beangle_commons = "org.beangle.commons" % "beangle-commons" % "5.6.28"
val beangle_web = "org.beangle.web" % "beangle-web" % "0.6.4"

val beangle_model = "org.beangle.data" % "beangle-model" % "5.8.23"
val beangle_cdi = "org.beangle.cdi" % "beangle-cdi" % "0.7.3"
val beangle_template = "org.beangle.template" % "beangle-template" % "0.1.26"
val beangle_doc_transfer = "org.beangle.doc" % "beangle-doc-transfer" % "0.4.12"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-webmvc",
    common,
    libraryDependencies ++= Seq(logback_classic % "test", scalatest, mockito),
    libraryDependencies ++= Seq(beangle_commons, beangle_web, javassist, scalaxml),
    libraryDependencies ++= Seq(beangle_template % "optional", beangle_cdi % "optional"),
    libraryDependencies ++= Seq(freemarker % "optional", hibernate_core % "optional"),
    libraryDependencies ++= Seq(spring_context % "optional", spring_beans % "optional"),
    libraryDependencies ++= Seq(beangle_model % "optional", beangle_doc_transfer % "optional")
  )
