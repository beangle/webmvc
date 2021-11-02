import org.beangle.parent.Dependencies._
import org.beangle.parent.Settings._

ThisBuild / organization := "org.beangle.webmvc"
ThisBuild / version := "0.4.9-SNAPSHOT"

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

ThisBuild / description := "The Beangle WebMVC Library"
ThisBuild / homepage := Some(url("https://beangle.github.io/webmvc/index.html"))

val beangle_commons_core = "org.beangle.commons" %% "beangle-commons-core" %  "5.2.9"
val beangle_commons_text = "org.beangle.commons" %% "beangle-commons-text" %  "5.2.9"

val beangle_data_hibernate = "org.beangle.data" %% "beangle-data-hibernate" % "5.3.26"
val beangle_data_transfer = "org.beangle.data" %% "beangle-data-transfer" % "5.3.26"

val beangle_cdi_api = "org.beangle.cdi" %% "beangle-cdi-api" % "0.3.4"
val beangle_cdi_spring = "org.beangle.cdi" %% "beangle-cdi-spring" % "0.3.4"

val beangle_cache_api = "org.beangle.cache" %% "beangle-cache-api" % "0.0.25"
val beangle_template_api = "org.beangle.template" %% "beangle-template-api" % "0.0.36"
val beangle_template_freemarker = "org.beangle.template" %% "beangle-template-freemarker" % "0.0.36"

val beangle_web_action = "org.beangle.web" %% "beangle-web-action" % "0.0.4"
val beangle_web_servlet = "org.beangle.web" %% "beangle-web-servlet" % "0.0.4"

val commonDeps = Seq(beangle_commons_core, beangle_commons_text, javassist, logback_classic, logback_core, scalatest, beangle_web_action, beangle_web_servlet)
val itext =Seq(itextpdf % "optional",itext_asian % "optional",itext_xmlworker % "optional")

lazy val root = (project in file("."))
  .settings()
  .aggregate(core,freemarker,support,bootstrap,showcase)

lazy val core = (project in file("core"))
  .settings(
    name := "beangle-webmvc-core",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(beangle_commons_text,javassist,beangle_cache_api,beangle_cdi_api,beangle_template_api,scalaxml))
  )

lazy val freemarker = (project in file("freemarker"))
  .settings(
    name := "beangle-webmvc-freemarker",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(beangle_template_freemarker))
  ).dependsOn(core)

lazy val support = (project in file("support"))
  .settings(
    name := "beangle-webmvc-support",
    common,
    libraryDependencies ++= (commonDeps ++ Seq(beangle_cdi_spring,beangle_data_transfer,beangle_data_hibernate,beangle_commons_text) ++ itext)
  ).dependsOn(core)

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
    libraryDependencies ++= (commonDeps ++  Seq(beangle_template_freemarker) )
  ).dependsOn(core,support,freemarker,bootstrap)

publish / skip := true
