package jp.ne.opt.sbt.diff

import sbt._
import Keys._
import complete.Parsers.spaceDelimited
import com.mayreh.sbt.dependency.SbtPlugin.autoImport.reverseDependency

object SbtPlugin extends AutoPlugin {
  object autoImport extends SbtPluginKeys

  import autoImport._

  override def trigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    gitDiffSeparator in Global := "\n",
    printGitDiffByBaseDirectory in Global := false,
    printGitDiffByAbsolutePath in Global := false,
    excludeRootProject in Global := true,
    gitDiff := {
      import sys.process._

      spaceDelimited("<arg>").parsed match {
        case commit1 +: commit2 +: Nil =>
          val project = thisProject.value
          val files = s"git diff --name-only $commit1 $commit2".lines_!.toList.collect {
            case line if line.trim.nonEmpty => new File(line)
          }

          if (files.exists(_.absolutePath.contains(project.base.absolutePath))) {
            project +: (reverseDependency in thisProjectRef).value
          } else {
            Nil
          }
        case _ => Nil
      }
    },
    commands += Command.args("git-diff-all", "<arg>") { (state, args) =>
      args match {
        case commit1 +: commit2 +: Nil =>
          val buildRoot = new File(loadedBuild.value.root.getPath)

          val (modifiedState, diffProjects) = loadedBuild.value.allProjectRefs.foldLeft(state -> Seq.empty[ResolvedProject]) {
            case ((currentState, projects), (ref, project)) =>
              val (s, resolvedProjects) = Project.extract(state).runInputTask(gitDiff in ref, s" $commit1 $commit2", state)
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
        case _ =>
          state
      }
    }
  )

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
      } mkString separator

      println(str)
    }
  }
}
