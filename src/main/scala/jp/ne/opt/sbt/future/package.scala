/**
 * The _runInputTask method is
 *
 * Copyright (c) 2008-2014 Typesafe Inc, Mark Harrah, Grzegorz Kossakowski, Josh Suereth, Indrajit Raychaudhuri, Eugene Yokota, and other contributors.
 * https://raw.githubusercontent.com/sbt/sbt/0.13/LICENSE
 *
 */

package jp.ne.opt.sbt

import sbt._
import sbt.complete.Parser

package object future {
  implicit class RichExtracted(val self: Extracted) extends AnyVal {
    def _runInputTask[T](key: InputKey[T], input: String, state: State): (State, T) = {
      import EvaluateTask._
      import self._

      val scopedKey = Scoped.scopedSetting(
        Scope.resolveScope(Load.projectScope(self.currentRef), currentRef.build, structure.rootProject)(key.scope), key.key)
      val rkey = Project.mapScope(Scope.resolveScope(GlobalScope, currentRef.build, rootProject))(scopedKey.scopedKey)
      val inputTask = get(Scoped.scopedSetting(rkey.scope, rkey.key))
      val task = Parser.parse(input, inputTask.parser(state)) match {
        case Right(t)  => t
        case Left(msg) => sys.error(s"Invalid programmatic input:\n$msg")
      }
      val config = extractedTaskConfig(self, structure, state)
      withStreams(structure, state) { str =>
        val nv = nodeView(state, str, rkey :: Nil)
        val (newS, result) = EvaluateTask.runTask(task, state, str, structure.index.triggers, config)(nv)
        (newS, processResult(result, newS.log))
      }
    }
  }
}
