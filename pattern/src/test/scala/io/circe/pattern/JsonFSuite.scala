package io.circe.pattern

import cats.instances.int._, cats.instances.option._, cats.instances.set._
import cats.laws.discipline.TraverseTests
import io.circe.Json
import io.circe.pattern.JsonF.{ foldJson, unfoldJson }

class JsonFSuite extends CirceSuite {
  checkLaws("Traverse[JsonF]", TraverseTests[JsonF].traverse[Int, Int, Int, Set[Int], Option, Option])

  "fold then unfold" should "be identity " in forAll { jsonF: JsonF[Json] =>
    assert(unfoldJson(foldJson(jsonF)) === jsonF)
  }

  "unfold then fold" should "be identity " in forAll { json: Json =>
    assert(foldJson(unfoldJson(json)) === json)
  }
}
