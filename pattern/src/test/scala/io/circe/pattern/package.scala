package io.circe

import io.circe.pattern.JsonF.{ ArrayF, BooleanF, NullF, NumberF, ObjectF, StringF }
import io.circe.testing.instances._
import org.scalacheck.{ Arbitrary, Gen, Shrink }

package object pattern {
  implicit def arbitraryJsonF[A: Arbitrary]: Arbitrary[JsonF[A]] =
    Arbitrary(
      Gen.oneOf[JsonF[A]](
        Arbitrary.arbitrary[Boolean].map(BooleanF),
        Arbitrary.arbitrary[JsonNumber].map(NumberF),
        Gen.const(NullF),
        Arbitrary.arbitrary[String].map(StringF),
        Arbitrary.arbitrary[Vector[(String, A)]].map(_.groupBy(_._1).mapValues(_.head._2).toVector).map(ObjectF.apply),
        Arbitrary.arbitrary[Vector[A]].map(ArrayF.apply)
      )
    )

  implicit def shrinkJsonF[A](implicit A: Shrink[A]): Shrink[JsonF[A]] = Shrink {
    case JsonF.NullF       => Stream.empty
    case JsonF.BooleanF(_) => Stream.empty
    case JsonF.NumberF(n)  => shrinkJsonNumber.shrink(n).map(JsonF.NumberF(_))
    case JsonF.StringF(s)  => Shrink.shrinkString.shrink(s).map(JsonF.StringF(_))
    case JsonF.ArrayF(values) =>
      Shrink.shrinkContainer[Vector, A].shrink(values).map(JsonF.ArrayF(_))
    case JsonF.ObjectF(fields) =>
      Shrink.shrinkContainer[Vector, (String, A)].shrink(fields).map(JsonF.ObjectF(_))
  }
}
