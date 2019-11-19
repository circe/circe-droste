package io.circe

import cats.kernel.Eq
import cats.~>
import higherkindness.droste.{ Algebra, Basis, Coalgebra, Delay }
import higherkindness.droste.syntax.compose.∘
import io.circe.pattern.JsonF

package object droste {
  val jsonAlgebra: Algebra[JsonF, Json] = Algebra(JsonF.foldJson)
  val jsonCoalgebra: Coalgebra[JsonF, Json] = Coalgebra(JsonF.unfoldJson)

  implicit val jsonBasis: Basis[JsonF, Json] = Basis.Default(jsonAlgebra, jsonCoalgebra)

  implicit val jsonDelayedEq: Delay[Eq, JsonF] =
    λ[Eq ~> (Eq ∘ JsonF)#λ](eq => JsonF.jsonFEqInstance(eq))
}
