import org.beangle.parent.Dependencies.*
import org.beangle.parent.Settings.*

ThisBuild / organization := "org.beangle.webmvc"
ThisBuild / version := "0.9.31-SNAPSHOT"

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

val bg_commons = "org.beangle.commons" % "beangle-commons" % "5.6.17"
val bg_model = "org.beangle.data" % "beangle-model" % "5.8.12"
val bg_cache = "org.beangle.cache" % "beangle-cache" % "0.1.9"
val bg_template = "org.beangle.template" % "beangle-template" % "0.1.17"
val bg_web = "org.beangle.web" % "beangle-web" % "0.4.12"
val bg_doc_transfer = "org.beangle.doc" % "beangle-doc-transfer" % "0.4.0"
val bg_cdi = "org.beangle.cdi" % "beangle-cdi" % "0.6.7"

lazy val root = (project in file("."))
  .settings(
    name := "beangle-webmvc",
    common,
    libraryDependencies ++= Seq(logback_classic % "test", scalatest),
    libraryDependencies ++= Seq(bg_commons, bg_web, javassist, bg_cache, bg_cdi, bg_template, scalaxml),
    libraryDependencies ++= Seq(freemarker % "optional", hibernate_core % "optional"),
    libraryDependencies ++= Seq(spring_context % "optional", spring_beans % "optional"),
    libraryDependencies ++= Seq(bg_model % "optional", bg_doc_transfer % "optional")
  )
