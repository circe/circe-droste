# circe-rs

[![Build status](https://img.shields.io/travis/circe/circe-rs/master.svg)](https://travis-ci.org/circe/circe-rs)
[![Coverage status](https://img.shields.io/codecov/c/github/circe/circe-rs/master.svg)](https://codecov.io/github/circe/circe-rs)
[![Gitter](https://img.shields.io/badge/gitter-join%20chat-green.svg)](https://gitter.im/circe/circe)
[![Maven Central](https://img.shields.io/maven-central/v/io.circe/circe-rs_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/io.circe/circe-rs_2.13)

This project includes some tools for working with [Circe][circe]'s representation of JSON documents using recursion
schemes. It currently includes a pattern functor for `io.circe.Json` and some basic integration with [Droste][droste].

## Usage

Count all the nulls anywhere in a document!

```scala
import higherkindness.droste.Algebra, higherkindness.droste.scheme.cata
import io.circe.rs.JsonF, io.circe.droste._, io.circe.literal._

val nullCounter: Algebra[JsonF, Int] = Algebra {
  case JsonF.JNullF => 1
  case JsonF.JArrayF(xs) => xs.sum
  case JsonF.JObjectF(fs) => fs.map(_._2).sum
  case _ => 0
}

val doc = json"""{"x":[null,{"y":[1,null,true,[null,null]]}]}"""

val result = cata(nullCounter).apply(doc) // result: Int = 4
```

Or you can use Droste's `foldMap`:

```scala
import cats.instances.int._
import higherkindness.droste.syntax.project._
import io.circe.droste._, io.circe.literal._

val doc = json"""{"x":[null,{"y":[1,null,true,[null,null]]}]}"""

val result = doc.foldMap(j => if (j.isNull) 1 else 0) // result: Int = 4
```

## Contributors and participation

This project supports the Scala [code of conduct][code-of-conduct] and we want
all of its channels (Gitter, GitHub, etc.) to be welcoming environments for everyone.

Please see the [Circe contributors' guide][contributing] for details on how to submit a pull
request.

## License

circe-rs is licensed under the **[Apache License, Version 2.0][apache]**
(the "License"); you may not use this software except in compliance with the
License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[apache]: http://www.apache.org/licenses/LICENSE-2.0
[api-docs]: https://circe.github.io/circe-rs/api/io/circe/
[circe]: https://github.com/circe/circe
[code-of-conduct]: https://www.scala-lang.org/conduct.html
[contributing]: https://circe.github.io/circe/contributing.html
[droste]: https://github.com/higherkindness/droste
