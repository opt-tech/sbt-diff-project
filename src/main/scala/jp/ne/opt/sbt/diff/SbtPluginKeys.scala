package jp.ne.opt.sbt.diff

import sbt._

trait SbtPluginKeys {
  val gitDiff = InputKey[Seq[ResolvedProject]]("git-diff",
    """Return which projects are different between two commits.
      |Usage: git-diff <commit> <commit>""".stripMargin)
  val gitDiffSeparator = SettingKey[String]("git-diff-separator",
    "Specify separator string for printing projects.")
  val printGitDiffByBaseDirectory = SettingKey[Boolean]("print-git-diff-by-base-directory",
    "Print base directory instead of project ID.")
  val printGitDiffByAbsolutePath = SettingKey[Boolean]("print-git-diff-by-absolute-path",
    "Print absolute path instead of related path. This option only affects when printGitDiffByBaseDirectory is true.")
  val excludeRootProject = SettingKey[Boolean]("exclude-root-project",
    "Specify if results should exclude root project for git-diff-all command.")
  val patternsAffectAllProjects = SettingKey[Seq[String]]("patterns-affect-all-projects",
    "Specify regex patterns. If some filename in `git diff` matches, gitDiff task  and git-diff-all command return all projects.")
}
