lazy val root = (project in file(".")).settings(
  sbtPlugin := true,
  crossSbtVersions := Vector("0.13.16", "1.0.2"),
  name := "sbt-diff-project",
  licenses += "MIT" -> url("https://raw.githubusercontent.com/opt-tech/sbt-diff-project/master/LICENSE"),
  addSbtPlugin("com.mayreh" % "sbt-reverse-dependency" % "0.2.0"),

  version := "0.2.1-SNAPSHOT",
  organization := "jp.ne.opt",
  organizationName := "OptTechnologies",
  startYear := Some(2016),

  /**
   * scripted test settings
   */
  scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
  scriptedBufferLog := false
)
