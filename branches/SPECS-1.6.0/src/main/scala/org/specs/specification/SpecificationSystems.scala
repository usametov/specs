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
 * DEALINGS IN THE SOFTWARE.
 */
package org.specs.specification

/**
 * This trait abstracts the building and storing of the systems of a Specification.
 */
trait SpecificationSystems { this: BaseSpecification =>
  /** list of systems under test */
  var systems : List[Sus] = Nil

  /**
   * implicit definition allowing to declare a new system under test described by a string <code>desc</code><br>
   * Usage: <code>"my system under test" should {}</code><br>
   * Alternatively, it could be created with:
   * <code>specify("my system under test").should {}</code>
   */
  implicit def specifySus(desc: String): SpecifiedSus = {
    new SpecifiedSus(addSus(new Sus(desc, this)))
  }
  def specify(desc: String): Sus = specifySus(desc).sus
  /** 
   * this class is used instead of using the Sus directly in order to make sure that only "should" and "can" are added
   * to Strings
   */
  class SpecifiedSus(val sus: Sus) {
    def should(a: =>Any) = sus.should(a)
    def should(a: =>Unit) = sus.should(a)
    def can(a: =>Any) = sus.can(a)
    def can(a: =>Unit) = sus.can(a)
  }
  /**
   * specifies an anonymous Sus included in this specification
   */
  def specify: Sus = {
    addSus(new Sus(this))
  }
  /** 
   * add a new Sus to this specification
   */
  private[specs] def addSus(sus: Sus): Sus = {
    addChild(sus)
    systems = systems ::: List(sus)
    if (this.isSequential) sus.setSequential
    sus
  }
  /**
   * add a textual complement to the sus verb.
   * For example, it is possible to declare:
   * <code>"the system" should provide {...}</code>
   * if the following function is declared:
   * <code>def provide = addToSusVerb("provide")</code>
   */
  def addToSusVerb(complement: String) = new Function1[Example, Example] {
    def apply(e: Example) = { 
      current match { 
        case Some(sus: Sus) => sus.verb += " " + complement 
        case _ => 
      }
      e
    }
  }
}
