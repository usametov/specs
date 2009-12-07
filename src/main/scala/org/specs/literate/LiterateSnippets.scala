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
package org.specs.literate
import org.specs.specification._
import org.specs.matcher._

/**
 * This trait adds the possibility to define and execute fragments of code in Literate specifications
 */
trait LiterateSnippets extends SnipIt with ExpectableFactory with Matchers { 
  def executeIs(s: String) = { execute(it) must include(s) }
  def executeIsNot(s: String) = execute(it) mustNot include(s)
  implicit def toOutputSnippet(s: String) = OutputSnippet(s)
  case class OutputSnippet(s: String) {
    def add_> = {
      s add it
      "> " + execute(it)
    } 
  }
  def >(s: String) = outputIs(s)
  def outputIs(s: String) = {
    val result = execute(it)
    var out = "> " + result
    try  { result must include(s) }
    catch { case e => out = "> " + e.getMessage }
    out
  }
}