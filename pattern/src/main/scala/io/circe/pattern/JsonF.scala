package io.circe.pattern

import cats.{ Applicative, Eval, Traverse }
import cats.instances.tuple._
import cats.instances.vector._
import cats.kernel.Eq
import cats.kernel.instances.string._
import io.circe.{ Json, JsonNumber, JsonObject }

/**
 * A pattern-functor reflecting the JSON datatype structure in a non-recursive way.
 */
sealed trait JsonF[+A] {
  def map[B](f: A => B): JsonF[B]
}

object JsonF {
  final case object NullF extends JsonF[Nothing] {
    def map[B](f: Nothing => B): JsonF[B] = this
  }
  final case class BooleanF(b: Boolean) extends JsonF[Nothing] {
    def map[B](f: Nothing => B): JsonF[B] = this
  }
  final case class NumberF(n: JsonNumber) extends JsonF[Nothing] {
    def map[B](f: Nothing => B): JsonF[B] = this
  }
  final case class StringF(s: String) extends JsonF[Nothing] {
    def map[B](f: Nothing => B): JsonF[B] = this
  }
  final case class ArrayF[A](value: Vector[A]) extends JsonF[A] {
    def map[B](f: A => B): JsonF[B] = ArrayF(value.map(f))
  }
  final case class ObjectF[A](fields: Vector[(String, A)]) extends JsonF[A] {
    def map[B](f: A => B): JsonF[B] = ObjectF(fields.map { case (k, v) => (k, f(v)) })
  }

  /**
   * An co-algebraic function that unfolds one layer of json into the pattern functor. Can be used for anamorphisms.
   */
  def unfoldJson(json: Json): JsonF[Json] = json.foldWith(unfolder)

  /**
   * An algebraic function that collapses one layer of pattern-functor into Json. Can be used for catamorphisms.
   */
  def foldJson(jsonF: JsonF[Json]): Json = jsonF match {
    case NullF           => Json.Null
    case BooleanF(bool)  => Json.fromBoolean(bool)
    case StringF(string) => Json.fromString(string)
    case NumberF(value)  => Json.fromJsonNumber(value)
    case ArrayF(vec)     => Json.fromValues(vec)
    case ObjectF(fields) => Json.obj(fields: _*)
  }

  private[this] type Field[A] = (String, A)
  private[this] type Fields[A] = Vector[(String, A)]
  private[this] val fieldInstance: Traverse[Fields] = catsStdInstancesForVector.compose[Field]

  implicit val jsonFTraverseInstance: Traverse[JsonF] = new Traverse[JsonF] {
    override def map[A, B](fa: JsonF[A])(f: A => B): JsonF[B] = fa.map(f)

    override def traverse[G[_], A, B](fa: JsonF[A])(f: A => G[B])(implicit G: Applicative[G]): G[JsonF[B]] = fa match {
      case NullF           => G.pure(NullF)
      case x @ BooleanF(_) => G.pure(x)
      case x @ StringF(_)  => G.pure(x)
      case x @ NumberF(_)  => G.pure(x)
      case ArrayF(vecA)    => G.map(catsStdInstancesForVector.traverse(vecA)(f))(vecB => ArrayF(vecB))
      case ObjectF(fieldsA) =>
        G.map(fieldInstance.traverse(fieldsA)(f))(fieldsB => ObjectF(fieldsB))
    }

    override def foldLeft[A, B](fa: JsonF[A], b: B)(f: (B, A) => B): B =
      fa match {
        case NullF        => b
        case BooleanF(_)  => b
        case StringF(_)   => b
        case NumberF(_)   => b
        case ArrayF(vecA) => vecA.foldLeft(b)(f)
        case ObjectF(fieldsA) =>
          fieldInstance.foldLeft(fieldsA, b)(f)
      }

    override def foldRight[A, B](fa: JsonF[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      fa match {
        case NullF        => lb
        case BooleanF(_)  => lb
        case StringF(_)   => lb
        case NumberF(_)   => lb
        case ArrayF(vecA) => catsStdInstancesForVector.foldRight(vecA, lb)(f)
        case ObjectF(fieldsA) =>
          fieldInstance.foldRight(fieldsA, lb)(f)
      }
  }

  implicit def jsonFEqInstance[A: Eq]: Eq[JsonF[A]] = Eq.instance {
    case (NullF, NullF)                       => true
    case (BooleanF(b1), BooleanF(b2))         => b1 == b2
    case (StringF(s1), StringF(s2))           => s1 == s2
    case (NumberF(jn1), NumberF(jn2))         => jn1 == jn2
    case (ArrayF(values1), ArrayF(values2))   => Eq[Vector[A]].eqv(values1, values2)
    case (ObjectF(values1), ObjectF(values2)) => Eq[Vector[(String, A)]].eqv(values1, values2)
    case _                                    => false
  }

  private[this] val unfolder: Json.Folder[JsonF[Json]] =
    new Json.Folder[JsonF[Json]] {
      def onNull = NullF
      def onBoolean(value: Boolean) = BooleanF(value)
      def onNumber(value: JsonNumber) = NumberF(value)
      def onString(value: String) = StringF(value)
      def onArray(value: Vector[Json]) = ArrayF(value)
      def onObject(value: JsonObject) =
        ObjectF(value.toVector)
    }
}
