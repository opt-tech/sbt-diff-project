lazy val root = (project in file(".")).settings(
  sbtPlugin := true,
  name := "sbt-diff-project",
  licenses += "MIT" -> url("https://raw.githubusercontent.com/opt-tech/sbt-diff-project/master/LICENSE"),
  addSbtPlugin("com.mayreh" % "sbt-reverse-dependency" % "0.1.1"),

  version := "0.1.1-SNAPSHOT",
  organization := "jp.ne.opt",
  organizationName := "OptTechnologies",
  startYear := Some(2016),

  /**
   * scripted test settings
   */
  scriptedSettings,
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false
)
