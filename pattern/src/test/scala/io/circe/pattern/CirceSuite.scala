package io.circe.pattern

import io.circe.testing.ArbitraryInstances
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.{ Checkers, ScalaCheckDrivenPropertyChecks }
import org.typelevel.discipline.Laws

trait CirceSuite extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks with ArbitraryInstances {
  def checkLaws(name: String, ruleSet: Laws#RuleSet): Unit = ruleSet.all.properties.zipWithIndex.foreach {
    case ((id, prop), 0) => name should s"obey $id" in Checkers.check(prop)
    case ((id, prop), _) => it should s"obey $id" in Checkers.check(prop)
  }
}
