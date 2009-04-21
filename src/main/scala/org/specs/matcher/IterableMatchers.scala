/**
 * Copyright (c) 2007-2009 Eric Torreborre <etorreborre@yahoo.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software. Neither the name of specs nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS INTHE SOFTWARE.
 */
package org.specs.matcher
import org.specs.matcher.MatcherUtils._
import org.specs.collection.ExtendedIterable._
import org.specs.matcher.AnyMatchers._
import org.specs.specification._
import org.specs.util.EditDistance._

/**
 * The <code>IterableMatchers</code> trait provides matchers which are applicable to Iterable objects
 */
trait IterableMatchers extends IterableBaseMatchers with IterableBeHaveMatchers
trait IterableBaseMatchers { outer =>

  /**
   * Matches if iterable.exists(_ == a)
   */
  def contain[T](a: T) = new Matcher[Iterable[Any]](){
    def apply(v: => Iterable[Any]) = {val iterable = v; (iterable.exists(_ == a), d(iterable) + " contains " + q(a), d(iterable) + " doesn't contain " + q(a))}
  }

  /**
   * Matches if not(iterable.exists(_ == a))
   */
  def notContain[T](a: T) = contain(a).not

  /**
   * Matches if all the elements of l are included in the actual iterable
   */
  def containAll[T](l: Iterable[T])(implicit details: Detailed) = new Matcher[Iterable[T]]() {
    def apply(v: => Iterable[T]) = {
      val iterable = v;
      import org.specs.Products._
      val failureMessage = details match {
        case full: fullDetails => EditMatrix(d(iterable.mkString("\n")), q(l.mkString("\n"))).showDistance(full.separators).toList.mkString(" doesn't contain all of ")
        case no: noDetails => d(iterable) + " doesn't contain all of " + q(l)
      }
      (l.forall(x => iterable.exists(_ == x)), d(iterable) + " contains all of " + q(l), failureMessage)
    }
  }

  /**
   * Alias for containAll.not
   */
  def notContainAll[T](l: Iterable[T])(implicit details: Detailed) = containAll(l)(details).not

  /**
   * Matches if all the elements of l are included in the actual iterable in that order
   */
  def containInOrder[T](l: Iterable[T])(implicit details: Detailed) = new Matcher[Iterable[T]](){
    def apply(v: => Iterable[T]) = {
      val iterable = v;
      import org.specs.Products._
      val failureMessage = details match {
        case full: fullDetails => EditMatrix(d(iterable.mkString("\n")), q(l.mkString("\n"))).showDistance(full.separators).toList.mkString("", " doesn't contain all of ", " in order")
        case no: noDetails => d(iterable) + " doesn't contain all of " + q(l) + " in order"
      }
      (iterable.containsInOrder(l), d(iterable) + " contains all of " + q(l) + " in order", failureMessage)
    }
  }

  /**
   * Matches if there is one element in the iterable verifying the <code>function</code> parameter: <code>(iterable.exists(function(_))</code>
   */
  def have[T](function: T => Boolean) = new Matcher[Iterable[T]](){
    def apply(v: => Iterable[T]) = {val iterable = v; (iterable.exists{function(_)}, "at least one element verifies the property in " + d(iterable), "no element verifies the property in " + d(iterable))}
  }
  /**
   * Matches if there is no element in the iterable verifying the <code>function</code> parameter: <code>!(iterable.exists(function(_))</code>
   */
  def notHave[T](function: T => Boolean) = have(function).not
  /**
   * Matches if there is one element in the iterable verifying the <code>function</code> parameter: <code>(iterable.exists(function(_))</code>
   * @deprecated  use have instead
   */
  def exist[T](function: T => Boolean) = have(function)

  /**
   * Matches if there is no element in the iterable verifying the <code>function</code> parameter: <code>!(iterable.exists(function(_))</code>
   * @deprecated use notHave instead instead
   */
  def notExist[T](function: T => Boolean) = notHave(function)
  /**
   * Alias for existMatch
   * @deprecated: use containMatch instead
   */
  def existMatch(pattern: String) = containMatch(pattern)
  /**
   * Matches if there is one element in the iterable[String] matching the <code>pattern</code> parameter: <code> iterable.exists(matches(pattern) _)</code>
   */
  def containMatch(pattern: String) = new Matcher[Iterable[String]](){
    def apply(v: => Iterable[String]) = {val iterable = v; (iterable.exists( matches(pattern) _), "at least one element matches " + q(pattern) + " in " + d(iterable), "no element matches " + q(pattern) + " in " + d(iterable))}
  }
  /**
   * Matches if not(existMatch(a))
   */
  def notContainMatch(pattern: String) = containMatch(pattern).not

  /**
   * Matches if there is exactly one element in the iterable[String] matching the <code>pattern</code> parameter</code>
   */
  def containMatchOnlyOnce(pattern: String) = new Matcher[Iterable[String]](){
    def apply(v: => Iterable[String]) = {
      val iterable = v;
      val matchNumber = iterable.filter( matches(pattern) _).toList.size
      (matchNumber == 1,
       "exactly one element matches " + q(pattern) + " in " + d(iterable),
       if (matchNumber == 0) "no element matches " + q(pattern) + " in " + d(iterable)
       else "more than one element matches " + q(pattern) + " in " + d(iterable))
    }
  }
  /**
   * Alias for notExistMatch
   * @deprecated: use notContainMatch instead
   */
  def notExistMatch(pattern: String) = existMatch(pattern).not

  /**
   * @deprecated: use haveTheSameElementsAs instead
   */
  def haveSameElementsAs[T](l: Iterable[T]) = haveTheSameElementsAs(l)
  /**
   * Matches if there l contains the same elements as the Iterable <code>iterable</code>.<br>
   * This verification does not consider the order of the elements but checks the iterables recursively
   */
  def haveTheSameElementsAs[T](l: Iterable[T]) = new HaveTheSameElementsAs(l)
  /**
   * @deprecated: use beTheSameSeqAs instead
   */
  def beSameSeqAs[T](s: =>Seq[T])(implicit d: Detailed) = beTheSameSeqAs(s)(d)
  /**
   * Matches if a sequence contains the same elements as s, using the equality (in the same order)
   */
  def beTheSameSeqAs[T](s: =>Seq[T])(implicit d: Detailed) = (toMatcher(AnyMatchers.be_==(_:T)(d)).toSeq)(s)

  /**
   * @deprecated: use beTheSameSetAs instead
   */
  def beSameSetAs[T](s: =>Set[T])(implicit d: Detailed) = beTheSameSetAs(s)(d)
  /**
   * Matches if a set contains the same elements as s, using the equality (in the any order)
   */
  def beTheSameSetAs[T](s: =>Set[T])(implicit d: Detailed) = (toMatcher(AnyMatchers.be_==(_:T)(d)).toSet)(s)

  /**
   * Matches if the size is n
   */
  def haveSize[T <% {def size: Int}](n: Int) = new Matcher[T](){
    def apply(v: => T) = {val collection = v; (collection.size == n, d(collection) + " has size " + n, d(collection) + " doesn't have size " + n)}
  }
}
trait IterableBeHaveMatchers { outer: IterableBaseMatchers =>
  /** 
   * matcher aliases and implicits to use with BeVerb and HaveVerb
   * unfortunately it cannot be made more generic w.r.t the container because this crashes the compiler
   * see Scala Trace #1864
   */
  /*
  implicit def toContainerResultMatcher[T, C[U] <: Iterable[U]](result: Result[C[T]]) = new ContainerResultMatcher[T, C](result)
  class ContainerResultMatcher[T, C[U] <: Iterable[U]](result: Result[C[T]]) {
    def size(i: Int) = result.matchWith(outer.size(i))
    def contain(a: T) = result.matchWith(outer.contain(a))
    def have(f: T =>Boolean) = result.matchWith(outer.have(f))
  }
  */
  implicit def toArrayResultMatcher[T](result: Result[Array[T]]) = new ArrayResultMatcher(result)
  class ArrayResultMatcher[T](result: Result[Array[T]]) {
    def size(i: Int) = result.matchWithMatcher(outer.size(i) ^^ ((t:Array[T]) => t.toList.asInstanceOf[Iterable[T]]))
    def contain(a: T) = result.matchWith(outer.contain(a))
    def have(f: T =>Boolean) = result.matchWith(outer.have(f) ^^ ((t:Array[T]) => t.toList.asInstanceOf[Iterable[T]]))
  }
  implicit def toListResultMatcher[T](result: Result[List[T]]) = new ListResultMatcher(result)
  class ListResultMatcher[T](result: Result[List[T]]) {
    def size(i: Int) = result.matchWithMatcher(outer.size(i))
    def contain(a: T) = result.matchWith(outer.contain(a))
    def have(f: T =>Boolean) = result.matchWith(outer.have(f) ^^ ((t:List[T]) => t.asInstanceOf[Iterable[T]]))
    def sameElementsAs(l: Iterable[T]) = result.matchWith(new HaveTheSameElementsAs(l))
  }
  implicit def toSeqResultMatcher[T](result: Result[Seq[T]]) = new SeqResultMatcher(result)
  class SeqResultMatcher[T](result: Result[Seq[T]]) {
    def size(i: Int) = result.matchWithMatcher(outer.size(i))
    def contain(a: T) = result.matchWith(outer.contain(a))
    def have(f: T =>Boolean) = result.matchWith(outer.have(f) ^^ ((t:Seq[T]) => t.asInstanceOf[Iterable[T]]))
    def sameSeqAs(s: =>Seq[T])(implicit d: Detailed) = result.matchWith(beTheSameSeqAs(s)) 
  }
  implicit def toSetResultMatcher[T](result: Result[Set[T]]) = new SetResultMatcher(result)
  class SetResultMatcher[T](result: Result[Set[T]]) {
    def size(i: Int) = result.matchWithMatcher(outer.size(i))
    def contain(a: T) = result.matchWith(outer.contain(a))
    def have(f: T =>Boolean) = result.matchWith(outer.have(f) ^^ ((t:Set[T]) => t.asInstanceOf[Iterable[T]]))
    def sameSetAs(s: =>Set[T])(implicit d: Detailed) = result.matchWithMatcher(outer.sameSetAs[T](s).asInstanceOf[Matcher[Set[T]]])
  }
  implicit def toIterableResultMatcher[T](result: Result[Iterable[T]]) = new IterableResultMatcher(result)
  class IterableResultMatcher[T](result: Result[Iterable[T]]) {
    def size(i: Int) = result.matchWithMatcher(outer.size(i))
    def contain(a: T) = result.matchWith(outer.contain(a))
    def have(f: T =>Boolean) = result.matchWith(outer.have(f))
  }
  implicit def toStringListResultMatcher(result: Result[List[String]]) = new StringListResultMatcher(result)
  class StringListResultMatcher(result: Result[List[String]]) {
    def containMatch(s: String) = result.matchWith(outer.containMatch(s))
  }
  def size[T](n: Int) = new Matcher[Iterable[T]](){
    def apply(v: => Iterable[T]) = {val collection = v.toList; (collection.size == n, d(collection) + " has size " + n, d(collection) + " doesn't have size " + n)}
  }
  def sameElementsAs[T](l: Iterable[T]) = new HaveTheSameElementsAs(l)
  def sameSeqAs[T](s: =>Seq[T])(implicit d: Detailed) = beTheSameSeqAs(s)(d).asInstanceOf[Matcher[Seq[T]]]
  def sameSetAs[T](s: =>Set[T])(implicit d: Detailed) = beTheSameSetAs(s)(d).asInstanceOf[Matcher[Set[T]]]
}
class HaveTheSameElementsAs[T] (l: Iterable[T]) extends Matcher[Iterable[T]] {
  def apply(it: => Iterable[T]) = {
    val iterable = it
    (l.sameElementsAs(iterable),
      d(l.toDeepString) + " has the same elements as " + q(iterable.toDeepString),
      d(l.toDeepString) + " doesn't have the same elements as " + q(iterable.toDeepString))
  }
}
