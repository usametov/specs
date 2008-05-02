package org.specs.specification
import org.specs.matcher.MatcherUtils._
import org.specs.SpecUtils._

/** 
 * This trait provides a structure to a specification.<br>
 * A specification is composed of:<ul>
 * <li>sub specifications or
 * <li>systems under tests (suts)
 * <li>examples which are components of systems under tests
 * <li>sub-examples which are components of examples</ul><p>
 * 
 * A specification is also given a description which is formed from its class name by default
 * but which can be also overriden<p>
 * 
 * A specification can be composed of other specifications:<br>
 * <code>"A complex specification".isSpecifiedBy(spec1, spec2)</code><br>
 * or <code>declare("A complex specification").isSpecifiedBy(spec1, spec2)</code>
 * <p>
 * A system under test can be created from a string with an implicit definition using <code>should</code>:<br>
 * <code>"my system under test" should {}</code><br>
 * Alternatively, it could be created with:
 * <code>specify("my system under test").should {}</code>
 * <p>
 * Sub-examples can be created by declaring them inside the current example:<pre>
 * def otherExample = "this is a shared example" in { "this assertion" must notBeEmpty }       
 *       
 * "behave like other examples" in {
 *  otherExample      
 * } </pre>
 * Sub-examples are usually used to share examples across specifications (see the Stack example in test/scala/scala/specs/sample)
 * <p>
 * A <code>SpecificationStructure</code> also implements an <code>ExampleLifeCycle</code> trait
 * allowing subclasses to refine the behaviour of the specification before/after an example and before/after 
 * a test inside an example. This is used to plug setup/teardown behaviour at the sut level and to plug
 * mock expectations checking when a specification is using the Mocker trait: <code>mySpec extends Specification with Mocker</code>
 */
trait SpecificationStructure extends ExampleLifeCycle with AssertFactory {

  /** description of the specification */ 
  var description = createDescription(getClass.getName)

  /** name of the specification */ 
  var name = createDescription(getClass.getName)

  /**
   * @return a description from the class name, taking the last name which doesn't contain a $ or a number.
   * For example: com.pack1.MyClass$1$ will:<ul>
   * <li>split on $ and reverse: [1, com.pack1.MyClass]
   * <li>drop the every element which is an integer -> [com.pack1.MyClass]
   * <li>take the first element: com.pack1.MyClass
   * <li>split on . and reverse: [MyClass, pack1, com]
   * <li>take the last element: MyClass</ul>
   */ 
  def createDescription(s: String) = s.
    split("\\$").reverse.
    dropWhile(isInteger(_))(0).
    split("\\.").
    reverse.toList(0)

  /** specifications contained by the current specification. An empty list by default */ 
  var subSpecifications: List[Specification] = Nil

  /** this declares that a specification is composed of other specifications */ 
  def isSpecifiedBy(specifications: Specification*) = {
    this.description = this.name + " is specified by"
    subSpecifications = subSpecifications:::specifications.toList
  }

  /** alias for isSpecifiedBy */ 
  def areSpecifiedBy(specifications: Specification*) = {
    this.description = this.name + " are specified by"
    subSpecifications = subSpecifications:::specifications.toList
  }

  /** 
   * implicit definition allowing to declare a composition inside the current specification:  
   * <code>"A complex specification".isSpecifiedBy(spec1, spec2)</code>
   */
  implicit def declare(d: String): SpecificationStructure = { name = d; this }
  
  /** list of systems under test */ 
  var suts : List[Sut] = Nil

  /** 
   * implicit definition allowing to declare a new system under test described by a string <code>desc</code><br>   
   * Usage: <code>"my system under test" should {}</code><br>
   * Alternatively, it could be created with:
   * <code>specify("my system under test").should {}</code>
   */
  implicit def specify(desc: String): Sut = { 
    suts = suts:::List(new Sut(desc, this))
    suts.last
  }

  /** utility method to track the last sut being currently defined, in order to be able to add examples to it */ 
  protected[this] def currentSut = if (!suts.isEmpty) suts.last else specify("The system")

  /** 
   * implicit definition allowing to declare a new example described by a string <code>desc</code><br>   
   * Usage: <code>"return 0 when asked for (0+0)" in {...}</code><br>
   * Alternatively, it could be created with:
   * <code>forExample("return 0 when asked for (0+0)").in {...}</code>
   */
  implicit def forExample(desc: String): Example = {
    val newExample = new Example(desc, currentSut)
    currentExamplesList += newExample 
    lastCreatedExample = newExample
    newExample
  }
  
  /** utility function to track the last example being currently defined, in order to be able to add assertions to it */ 
  protected[this] def lastExample: Example = if (lastCreatedExample == null) forExample("example") else lastCreatedExample
  protected[this] var lastCreatedExample: Example = _

  /** 
   * utility method to track the last example list being currently defined.<br>
   * It is either the list of examples associated with the current sut, or
   * the list of subexamples of the current example being defined 
   */ 
  protected[this] def currentExamplesList = currentSut.examples.find { _.isInsideDefinition } match {
    case Some(parentExample) => parentExample.subExamples
    case None => currentSut.examples
  }
}
trait ExampleLifeCycle {
  def beforeExample(ex: Example) = {} 
  def beforeTest(ex: Example)= {}
  def afterTest(ex: Example) = {}
  def executeTest(t: =>Any) = t
  def afterExample(ex: Example) = {}
}