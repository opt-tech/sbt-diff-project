# sbt-diff-project

### Make CI for sbt multi-project FASTER.

Show projects and their children which are different in git diff.

[![Circle CI](https://circleci.com/gh/opt-tech/sbt-diff-project.svg?style=shield)](https://circleci.com/gh/opt-tech/sbt-diff-project)

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
$ sbt --error 'set showSuccess := false' 'git-diff-all master feature/batch' # suppress sbt debug log
batch
secondBatch
```

Voila, `git-diff-all` command prints `batch`(contains modified file) and `secondBatch`(is dependsOn `batch`).

Now, you can test `batch` and `secondBatch` via pipe or redirection.

## Usage

### Installation

Add the plugin in project/plugins.sbt:

```scala
addSbtPlugin("jp.ne.opt" % "sbt-diff-project" % "0.1.0")
```

### `git-diff-all` command

`git-diff-all` sbt command executes `git diff --name-only` internally.

You can use this command as follows.

```bash
$ sbt git-diff-all
$ sbt 'git-diff-all 752a93 b2f98f'
$ sbt 'git-diff-all master feature/foo'
$ sbt --error 'set showSuccess := false' git-diff-all  # suppress sbt debug log
```

### Configurations

- `gitDiffSeparator` : Specify separator string for printing projects. `\n` as default.
- `printGitDiffByBaseDirectory` : Print base directory instead of project ID. `false` as default.
- `printGitDiffByAbsolutePath` : Print absolute path instead of related path. (only affects when `printGitDiffByBaseDirectory` is true) `false` by default.
- `excludeRootProject` : This plugin derives project-diff based on project's base directory. Since root project's base directory-path is included to any project's, excluding root project from diff is reasonable. `true` by default.
- `patternsAffectAllProjects` : For some special files, you would want to force testing all projects if the file is modified. (e.g. `.travis.yml`, `circle.yml`, `build.sbt`, ...) `Seq(""".+\.sbt$""", """.+project/[^/]+\.scala""")` by default.

## License

Published under the MIT License.
