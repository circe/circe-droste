package io.circe.pattern

import io.circe.testing.ArbitraryInstances
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.typelevel.discipline.scalatest.FlatSpecDiscipline

trait CirceSuite extends AnyFlatSpec with FlatSpecDiscipline with ScalaCheckDrivenPropertyChecks with ArbitraryInstances
