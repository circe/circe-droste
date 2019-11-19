package io.circe.droste

import higherkindness.droste.laws.BasisLaws
import io.circe.Json
import io.circe.pattern.JsonF
import io.circe.testing.instances._
import org.scalacheck.Properties

class DrosteSuite extends Properties("JsonF") {
  include(BasisLaws.props[JsonF, Json]("JsonF", "Json"))
}
