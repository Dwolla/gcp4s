ThisBuild / baseVersion := "0.1"

ThisBuild / organization := "com.armanbilge"
ThisBuild / publishGithubUser := "armanbilge"
ThisBuild / publishFullName := "Arman Bilge"
ThisBuild / startYear := Some(2021)

ThisBuild / homepage := Some(url("https://github.com/armanbilge/gcp4s"))
ThisBuild / scmInfo := Some(
  ScmInfo(url("https://github.com/armanbilge/gcp4s"), "git@github.com:armanbilge/gcp4s.git"))
sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / githubWorkflowEnv += "SERVICE_ACCOUNT_CREDENTIALS" -> "${{ secrets.SERVICE_ACCOUNT_CREDENTIALS }}"

replaceCommandAlias(
  "ci",
  "; project /; headerCheckAll; scalafmtCheckAll; scalafmtSbtCheck; clean; testIfRelevant; mimaReportBinaryIssuesIfRelevant"
)
replaceCommandAlias(
  "release",
  "; reload; project /; +mimaReportBinaryIssuesIfRelevant; +publishIfRelevant; sonatypeBundleRelease"
)
addCommandAlias("prePR", "; root/clean; +root/scalafmtAll; scalafmtSbt; +root/headerCreate")

val Scala3 = "3.1.0"
ThisBuild / crossScalaVersions := Seq(Scala3)

val CatsVersion = "2.7.0"
val CatsEffectVersion = "3.3.3"
val CirceVersion = "0.15.0-M1"
val Fs2Version = "3.2.4"
val Http4sVersion = "0.23.7"
val Log4CatsVersion = "2.1.1"
val MonocleVersion = "3.1.0"
val MunitVersion = "0.7.29"
val MunitCE3Version = "1.0.7"
val NatchezVersion = "0.1.6"
val ScalaCheckEffectMunitVersion = "1.0.3"
val ScodecBitsVersion = "1.1.30"
val ShapelessVersion = "3.0.4"

val commonSettings = Seq(
  scalacOptions ++=
    Seq("-new-syntax", "-indent", "-source:future", "-Xmax-inlines", "64"),
  sonatypeCredentialHost := "s01.oss.sonatype.org"
)

val commonJVMSettings = Seq(
  fork := true
)
val commonJSSettings = Seq(
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
)

lazy val root =
  project
    .aggregate(core.jvm, core.js, bigQuery.jvm, bigQuery.js, trace.jvm, trace.js)
    .enablePlugins(NoPublishPlugin)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .in(file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "gcp4s",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % CatsVersion,
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "co.fs2" %%% "fs2-io" % Fs2Version,
      "org.http4s" %%% "http4s-client" % Http4sVersion,
      "org.http4s" %%% "http4s-circe" % Http4sVersion,
      "io.circe" %%% "circe-parser" % CirceVersion,
      "io.circe" %%% "circe-scodec" % CirceVersion,
      "org.scodec" %%% "scodec-bits" % ScodecBitsVersion,
      "org.scalameta" %%% "munit" % MunitVersion % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % MunitCE3Version % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % ScalaCheckEffectMunitVersion % Test,
      "org.http4s" %%% "http4s-dsl" % Http4sVersion % Test,
      "org.http4s" %%% "http4s-ember-client" % Http4sVersion % Test
    ),
    buildInfoRenderFactory := sbtbuildinfo.Scala3CaseObjectRenderer.apply,
    buildInfoPackage := "gcp4s",
    buildInfoOptions += BuildInfoOption.PackagePrivate
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.10" % Test
    )
  )
  .settings(commonSettings)
  .jvmSettings(commonJVMSettings)
  .jsSettings(commonJSSettings)

lazy val bigQuery = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("bigquery"))
  .enablePlugins(DiscoveryPlugin)
  .settings(
    name := "gcp4s-bigquery",
    discoveryPackage := "gcp4s.bigquery",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "shapeless3-deriving" % ShapelessVersion,
      "dev.optics" %%% "monocle-core" % MonocleVersion,
      "io.circe" %%% "circe-testing" % CirceVersion % Test
    )
  )
  .settings(commonSettings)
  .jvmSettings(commonJVMSettings)
  .jsSettings(commonJSSettings)
  .dependsOn(core % "compile->compile;test->test")

lazy val trace = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("trace"))
  .enablePlugins(DiscoveryPlugin)
  .settings(
    name := "gcp4s-trace",
    discoveryPackage := "gcp4s.trace",
    libraryDependencies ++= Seq(
      "org.tpolecat" %%% "natchez-core" % NatchezVersion,
      "org.typelevel" %%% "log4cats-core" % Log4CatsVersion
    )
  )
  .settings(commonSettings)
  .jvmSettings(commonJVMSettings)
  .jsSettings(commonJSSettings)
  .dependsOn(core % "compile->compile;test->test")
