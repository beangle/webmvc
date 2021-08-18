import sbt.Keys._
import sbt._

object BuildSettings {
  val buildScalaVersion = "3.0.1"

  val commonSettings = Seq(
    organizationName := "The Beangle Software",
    licenses += ("GNU Lesser General Public License version 3", new URL("http://www.gnu.org/licenses/lgpl-3.0.txt")),
    startYear := Some(2005),
    scalaVersion := buildScalaVersion,
    scalacOptions := Seq("-Xtarget:11", "-deprecation", "-feature"),
    crossPaths := true,

    publishMavenStyle := true,
    publishConfiguration := publishConfiguration.value.withOverwrite(true),
    publishM2Configuration := publishM2Configuration.value.withOverwrite(true),
    publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),

    versionScheme := Some("early-semver"),
    pomIncludeRepository := { _ => false }, // Remove all additional repository other than Maven Central from POM
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
    })
}

object Dependencies {
  val logbackVer = "1.2.4"
  val scalatestVer = "3.2.9"
  val scalaxmlVer = "2.0.1"
  val commonsVer = "5.2.5"
  val dataVer = "5.3.24"
  val cdiVer = "0.3.1"
  val webVer = "0.0.1"
  val springVer = "5.3.6"
  val hibernateVer = "5.5.6.Final"
  val cacheVer= "0.0.23"
  val templateVer ="0.0.33"
  val javaassitVer = "3.27.0-GA"
  val itextVer="5.5.11"
  val itextAsianVer ="5.2.0"


  val scalatest = "org.scalatest" %% "scalatest" % scalatestVer % "test"
  val scalaxml = "org.scala-lang.modules" %% "scala-xml" % scalaxmlVer
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVer % "test"
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVer % "test"

  val commonsCore = "org.beangle.commons" %% "beangle-commons-core" % commonsVer
  val commonsText = "org.beangle.commons" %% "beangle-commons-text" % commonsVer

  val dataHibernate = "org.beangle.data" %% "beangle-data-hibernate" % dataVer
  val dataTransfer = "org.beangle.data" %% "beangle-data-transfer" % dataVer

  val cdiApi = "org.beangle.cdi" %% "beangle-cdi-api" % cdiVer
  val cdiSpring = "org.beangle.cdi" %% "beangle-cdi-spring" % cdiVer

  val cacheApi = "org.beangle.cache" %% "beangle-cache-api" % cacheVer
  val templateApi = "org.beangle.template" %% "beangle-template-api" % templateVer
  val templateFreemarker = "org.beangle.template" %% "beangle-template-freemarker" % templateVer

  val webAction = "org.beangle.web" %% "beangle-web-action" % webVer
  val webServlet = "org.beangle.web" %% "beangle-web-servlet" % webVer

  val javassist = "org.javassist" % "javassist" % javaassitVer

  val itextpdf="com.itextpdf" % "itextpdf" % itextVer % "optional"
  val itextAsian="com.itextpdf" % "itext-asian" % itextAsianVer % "optional"
  val itextXmlworker="com.itextpdf.tool" % "xmlworker" % itextVer % "optional"

  var commonDeps = Seq(commonsCore, commonsText, javassist, logbackClassic, logbackCore, scalatest, webAction, webServlet)
  val itext =Seq(itextpdf,itextAsian,itextXmlworker)
}

