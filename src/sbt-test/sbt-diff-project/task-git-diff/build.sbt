import java.io.PrintStream

/**
 * test projects
 */
lazy val root = (project in file("."))
  .settings(checkTasks: _*)

lazy val common = project
lazy val web = project.dependsOn(common)
lazy val batchCommon = project.dependsOn(common)
lazy val batch1 = project.dependsOn(batchCommon)
lazy val batch2 = project.dependsOn(batchCommon)

/**
 * check tasks
 */
def assertProjects(expected: Seq[String])(actual: Seq[ResolvedProject]): Unit = {
  val expectedSet = expected.toSet
  val actualSet = actual.map(_.id).toSet
  assert(expectedSet == actualSet, s"projects should be $expectedSet but were $actualSet")
}

lazy val diff_batchCommon = TaskKey[Seq[ResolvedProject]]("diff_batchCommon")
lazy val diff_common = TaskKey[Seq[ResolvedProject]]("diff_common")
lazy val diff_web = TaskKey[Seq[ResolvedProject]]("diff_web")
lazy val diff_affectAll = TaskKey[Seq[ResolvedProject]]("diff_affectAll")
lazy val diff_all = TaskKey[Seq[ResolvedProject]]("diff_all")

diff_batchCommon := (gitDiff in batchCommon).toTask(s" master branch-batchCommon").value
diff_common := (gitDiff in common).toTask(s" master branch-common").value
diff_web := (gitDiff in web).toTask(s" master branch-web").value
diff_affectAll := (gitDiff in common).toTask(s" master branch-affectAll").value
diff_all := gitDiff.toTask(s" master branch-all").value
excludeRootProject := true

lazy val checkTasks = Seq(
  TaskKey[Unit]("check-batchCommon") :=
    assertProjects(Seq("batchCommon", "batch1", "batch2"))(diff_batchCommon.value),
  TaskKey[Unit]("check-common") :=
    assertProjects(Seq("common", "web", "batchCommon", "batch1", "batch2"))(diff_common.value),
  TaskKey[Unit]("check-web") :=
    assertProjects(Seq("web"))(diff_web.value),
  TaskKey[Unit]("check-affectAll") :=
    assertProjects(Seq("common", "web", "batchCommon", "batch1", "batch2"))(diff_affectAll.value),
  TaskKey[Unit]("check-all") := {
    val logFile = new File(loadedBuild.value.root.getPath, "all.log")

    try {
      scala.Console.withOut(new PrintStream(logFile)) {
        Command.process("git-diff-all master branch-all", state.value)
      }
      val projectIds = IO.read(logFile).split('\n').filter(_.nonEmpty).toSet
      val expected = Set("batch2", "web")
      assert(projectIds == expected, s"projects should be $expected but were $projectIds")
    } finally {
      if (logFile.exists()) logFile.delete()
    }
  }
)
