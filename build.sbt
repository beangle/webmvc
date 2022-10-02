import org.beangle.parent.Dependencies._
import org.beangle.parent.Settings._

ThisBuild / organization := "org.beangle.webmvc"
ThisBuild / version := "0.6.2-SNAPSHOT"

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

val bg_commons_ver = "5.4.0"
val bg_data_ver = "5.5.2"
val bg_cdi_ver = "0.4.0"
val bg_cache_ver = "0.1.0"
val bg_template_ver = "0.1.0"
val bg_web_ver = "0.2.0"

val bg_commons_core = "org.beangle.commons" %% "beangle-commons-core" % bg_commons_ver
val bg_commons_text = "org.beangle.commons" %% "beangle-commons-text" % bg_commons_ver

val bg_data_orm = "org.beangle.data" %% "beangle-data-orm" % bg_data_ver
val bg_data_transfer = "org.beangle.data" %% "beangle-data-transfer" % bg_data_ver

val bg_cdi_api = "org.beangle.cdi" %% "beangle-cdi-api" % bg_cdi_ver
val bg_cdi_spring = "org.beangle.cdi" %% "beangle-cdi-spring" % bg_cdi_ver

val bg_cache_api = "org.beangle.cache" %% "beangle-cache-api" % bg_cache_ver
val bg_template_api = "org.beangle.template" %% "beangle-template-api" % bg_template_ver
val bg_template_freemarker = "org.beangle.template" %% "beangle-template-freemarker" % bg_template_ver

val bg_web_action = "org.beangle.web" %% "beangle-web-action" % bg_web_ver
val bg_web_servlet = "org.beangle.web" %% "beangle-web-servlet" % bg_web_ver

val commonDeps = Seq(bg_commons_core, bg_commons_text, javassist, logback_classic % "test", logback_core % "test",
  scalatest, bg_web_action, bg_web_servlet)
val itext = Seq(itextpdf % "optional", itext_asian % "optional", itext_xmlworker % "optional")

lazy val root = (project in file("."))
  .settings()
  .aggregate(core, spring, freemarker, support, bootstrap, showcase)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-webmvc-core",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(bg_commons_text, javassist, bg_cache_api, bg_cdi_api, bg_template_api, scalaxml))
  )

lazy val freemarker = (project in file("freemarker"))
  .settings(
    name := "beangle-webmvc-freemarker",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(bg_template_freemarker))
  ).dependsOn(core)

lazy val spring = (project in file("spring"))
  .settings(
    name := "beangle-webmvc-spring",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(bg_cdi_spring))
  ).dependsOn(core)

lazy val support = (project in file("support"))
  .settings(
    name := "beangle-webmvc-support",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(bg_data_transfer, bg_data_orm, bg_commons_text) ++ itext)
  ).dependsOn(core, spring)

lazy val bootstrap = (project in file("bootstrap"))
  .settings(
    name := "beangle-webmvc-bootstrap",
    common,
    libraryDependencies ++= commonDeps
  )

lazy val showcase = (project in file("showcase"))
  .settings(
    name := "beangle-webmvc-showcase",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(bg_template_freemarker))
  ).dependsOn(core, spring, support, freemarker, bootstrap)

publish / skip := true
