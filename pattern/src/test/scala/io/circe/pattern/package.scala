package io.circe

import io.circe.pattern.JsonF.{ JArrayF, JBooleanF, JNullF, JNumberF, JObjectF, JStringF }
import io.circe.testing.instances._
import org.scalacheck.{ Arbitrary, Gen, Shrink }

package object pattern {
  implicit def arbitraryJsonF[A: Arbitrary]: Arbitrary[JsonF[A]] =
    Arbitrary(
      Gen.oneOf[JsonF[A]](
        Arbitrary.arbitrary[Boolean].map(JBooleanF),
        Arbitrary.arbitrary[JsonNumber].map(JNumberF),
        Gen.const(JNullF),
        Arbitrary.arbitrary[String].map(JStringF),
        Arbitrary.arbitrary[Vector[(String, A)]].map(_.groupBy(_._1).mapValues(_.head._2).toVector).map(JObjectF.apply),
        Arbitrary.arbitrary[Vector[A]].map(JArrayF.apply)
      )
    )

  implicit def shrinkJsonF[A](implicit A: Shrink[A]): Shrink[JsonF[A]] = Shrink {
    case JsonF.JNullF       => Stream.empty
    case JsonF.JBooleanF(_) => Stream.empty
    case JsonF.JNumberF(n)  => shrinkJsonNumber.shrink(n).map(JsonF.JNumberF(_))
    case JsonF.JStringF(s)  => Shrink.shrinkString.shrink(s).map(JsonF.JStringF(_))
    case JsonF.JArrayF(values) =>
      Shrink.shrinkContainer[Vector, A].shrink(values).map(JsonF.JArrayF(_))
    case JsonF.JObjectF(fields) =>
      Shrink.shrinkContainer[Vector, (String, A)].shrink(fields).map(JsonF.JObjectF(_))
  }
}
