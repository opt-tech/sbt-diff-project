package jp.ne.opt.sbt.diff

import sbt._
import Keys._
import complete.Parsers.spaceDelimited
import com.mayreh.sbt.dependency.SbtPlugin.autoImport.reverseDependency
import jp.ne.opt.sbt.future._

object SbtPlugin extends AutoPlugin {
  object autoImport extends SbtPluginKeys

  import autoImport._

  override def trigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    gitDiffSeparator in Global := "\n",
    printGitDiffByBaseDirectory in Global := false,
    printGitDiffByAbsolutePath in Global := false,
    excludeRootProject in Global := true,
    patternsAffectAllProjects in Global := Seq(
      """.+\.sbt$""",
      """.+project/[^/]+\.scala"""
    ),
    gitDiff := {
      import sys.process._

      val project = thisProject.value
      val files = s"git diff --name-only ${spaceDelimited("<arg>").parsed.mkString(" ")}".lines_!.toList.collect {
        case line if line.trim.nonEmpty => new File(line)
      }

      if (affectsAll(patternsAffectAllProjects.value, files) || files.exists(_.absolutePath.contains(project.base.absolutePath))) {
        project +: (reverseDependency in thisProjectRef).value
      } else {
        Nil
      }
    },
    commands += Command.args("git-diff-all", "<arg>") { (state, args) =>
      val buildRoot = new File(loadedBuild.value.root.getPath)

      val (modifiedState, diffProjects) = loadedBuild.value.allProjectRefs.foldLeft(state -> Seq.empty[ResolvedProject]) {
        case ((currentState, projects), (ref, project)) =>
          val (s, resolvedProjects) = Project.extract(state)._runInputTask(gitDiff in ref, s" ${args.mkString(" ")}", state)
          s -> (projects ++ resolvedProjects)
      }

      /**
       * remove duplicates and filter root project
       */
      val filteredProjects = diffProjects
        .groupBy(_.id)
        .flatMap { case (_, xs) => xs.headOption }
        .filter { project =>
          !excludeRootProject.value || project.base.absolutePath != buildRoot.absolutePath
        }

      printProjects(filteredProjects.toSeq,
        buildRoot,
        gitDiffSeparator.value,
        printGitDiffByBaseDirectory.value,
        printGitDiffByAbsolutePath.value)

      modifiedState
    }
  )

  private[this] def affectsAll(patterns: Seq[String], files: Seq[File]): Boolean =
    files.exists(file => patterns.exists(pattern => file.absolutePath.matches(pattern)))

  private[this] def printProjects(projects: Seq[ResolvedProject],
                                  buildRoot: File,
                                  separator: String,
                                  byBaseDirectory: Boolean,
                                  byAbsolutePath: Boolean): Unit = {
    projects.headOption.foreach { _ =>
      val str = projects.map { project =>
        if (!byBaseDirectory) {
          project.id
        } else if (byAbsolutePath) {
          project.base.getAbsolutePath
        } else {
          buildRoot.toPath.relativize(project.base.toPath).toString
        }
      }.sorted mkString separator

      println(str)
    }
  }
}
