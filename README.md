# sbt-diff-project

### Make CI for sbt multi-project FASTER.

Show projects and their children which are different in git diff.

## Use case

Imagine that your sbt multi-project is defined as below.

```scala
lazy val root = project in file(".")

lazy val core = project
lazy val web = project.dependsOn(core)
lazy val batch = project.dependsOn(core)
lazy val secondBatch = project.dependsOn(batch)
```

Then, suppose that followings.

- You write unit tests to all projects.
- You added this multi-project build to Git version control and commit to `master` branch.
- `master` branch passed the tests.

If you modified a file in `batch` project, you need to test `batch` and `secondBatch` project only.

This can be achieved as follows. (suppose that changes are committed to `feature/batch` branch)

```bash
$ cd /path/to/sbt-project
$ sbt 'git-diff-all master feature/batch'
batch
secondBatch
```

You can test only `batch` and `secondBatch` via piping or redirecting this output. (testing directly is currently not supported.)

## Usage

### Installation

Add the plugin in project/plugins.sbt:

```scala
addSbtPlugin("jp.ne.opt" % "sbt-diff-project" % "0.1.0")
```

### Print diff

See above use case.

### Configurations

- `gitDiffSeparator` : Specify separator string for printing projects. `\n` as default.
- `printGitDiffByBaseDirectory` : Print base directory instead of project ID. `false` as default.
- `printGitDiffByAbsolutePath` : Print absolute path instead of related path. (only affects when `printGitDiffByBaseDirectory` is true) `false` by default.
- `excludeRootProject` : This plugin derives diff based on project's base directory. Since root project's base is included to all project's base, excluding root project is reasonable. `true` by default.
- `patternsAffectAllProjects` : For some special files, you would want to test all projects if the file is modified. (e.g. `.travis.yml`, `circle.yml`, `build.sbt`, ...) `Seq(""".+\.sbt$""", """.+project/[^/]+\.scala""")` by default.

## License

Published under the MIT License.
