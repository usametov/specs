package org.specs.runner
import org.specs.io._
import java.util.Calendar
import org.specs.specification._
import org.specs.util._
import org.specs._
import org.specs.specification._
import org.specs.ExtendedThrowable._

/**
 * This trait reports the result of a specification on a simple <code>Output</code>
 * which must support <code>print</code>-like methods
 */  
trait OutputReporter extends Reporter with Output {
  
  /** the timer is used to display execution times */
  val timer: Timer

  /** 
   * override the parent method for arguments setting and
   * call the local report method with no padding to being with.
   */
  override def report(specs: Seq[Specification]): this.type = {
    super.report(specs)
    report(specs, "")
  }

  /**
   * reports a list of specifications with a given space separator to display before the results.<br>
   * This method may be called recursively by the <code>reportSpec</code> method if a specification
   * has subSpecifications, hence the <code>padding</code> will be incremented
   */
  def report(specs: Seq[Specification], padding: String): this.type = { 
    specs foreach (reportSpec(_, padding))
    this
  }

  /**
   * reports a specification with a given space separator to display before the results.<br>
   * This method may be called recursively by the <code>reportSpec</code> method if a specification
   * has subSpecifications, hence the <code>padding</code> will be incremented
   */
  def reportSpec(spec: Specification, padding: String): this.type = {
    timer.start
    println(padding + "Specification \"" + spec.name + "\"")
    report(spec.subSpecifications, padding + "  ")
    reportSystems(spec.systems, padding + "  ")
    
    println(padding + "Total for specification \"" + spec.name + "\":")
    printStats(stats(spec), padding)   
    this
  }
   
  /** utility implicit definition to be able to add tuples */ 
  implicit def toAddableTuple(t1: Tuple5[Int, Int, Int, Int, Int]) = new AddableTuple(t1)
  class AddableTuple(t1: Tuple5[Int, Int, Int, Int, Int]) {  def +(t2: Tuple5[Int, Int, Int, Int, Int]) = (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3, t1._4 + t2._4, t1._5 + t2._5) }
  
  /**
   * @return the number of examples, assertions, failures and errors for a specification
   * by collecting those numbers on sub-specifications and systems
   */
  def stats(spec: Specification): (Int, Int, Int, Int, Int) = {
    spec.systems.foldLeft((0, 0, 0, 0, 0))(_ + stats(_)) +
    spec.subSpecifications.foldLeft((0, 0, 0, 0, 0))(_ + stats(_))
  }
  
  /**
   * @return the number of examples, assertions, failures and errors for a sus
   * by collecting those numbers on examples
   */
  def stats(sus: Sus): (Int, Int, Int, Int, Int)  = {
    sus.examples.foldLeft((0, 0, 0, 0, 0))(_ + stats(_))
  }

  /**
   * @return the number of examples, assertions, failures and errors for an example
   * by collecting those numbers on this example and on sub-examples
   */
  def stats(example: Example): (Int, Int, Int, Int, Int) = {
    (if (example.subExamples.isEmpty) 1 else 0, example.assertionsNb, example.failures.size, example.errors.size, example.skipped.size) +
    example.subExamples.foldLeft((0, 0, 0, 0, 0))(_ + stats(_))
  }

  /**
   * reports the sus results. If there are more than one, then report stats for each
   * else just print the specification of the sus, the parent specification will display the total
   * for that sus
   */
  def reportSystems(systems: Iterable[Sus], padding: String) = {
    if (systems.toList.size > 1) 
      systems foreach {reportSus(_, padding)}
    else
      systems foreach {printSus(_, padding)}
  }

  /**
   * reports one sus results: print the sus specifications, then the statistics
   */
  def reportSus(sus: Sus, padding: String) = { timer.start; printSus(sus, padding); printStats(sus, padding) }

  /**
   * prints one sus specification
   */
  def printSus(sus: Sus, padding: String) = {
    println(padding + sus.description + " " + sus.verb + sus.skippedSus.map(" (skipped: " + _ + ")").getOrElse(""))
    sus.literateDescription map { desc => println(padding + new TextFormatter().format(desc, sus.examples).text) }
    reportExamples(sus.examples, padding)
    println("")
  }
  /**
   * prints the statistics for a sus
   */
  def printStats(sus: Sus, padding: String): Unit = {
    println(padding + "Total for SUT \"" + sus.description + "\":")
    printStats(stats(sus), padding)    
  }
  
  /**
   * prints the statistics for a specification
   */
  def printStats(stat: (Int, Int, Int, Int, Int), padding: String) = {
    val (examplesNb, assertionsNb,  failuresNb, errorsNb, skippedNb) = stat
    def plural[T](nb: Int) = if (nb > 1) "s" else ""
    println(padding + "Finished in " + timer.stop)
    println(padding + 
            examplesNb + " example" + plural(examplesNb) +
            (if (skippedNb > 0) " (" + skippedNb + " skipped)" else "") + ", " +
            assertionsNb + " assertion" + plural(assertionsNb) + ", " +
            failuresNb + " failure" + plural(failuresNb) + ", " +
            errorsNb + " error" + plural(errorsNb)
            )
    println("")
  }

  /**
   * reports a list of examples and indent subexamples if there are some
   */
  def reportExamples(examples: Iterable[Example], padding: String): Unit = {
    for (example <- examples) {
      reportExample(example, padding)
      reportExamples(example.subExamples, padding + "  ")
    }
  }

  /**
   * reports one example: + if it succeeds, x if it fails, its description, its failures or errors 
   */
  def reportExample(example: Example, padding: String) = {
    def status(example: Example) = {
      if (example.errors.size + example.failures.size > 0) 
        "x " 
      else if (example.skipped.size > 0)
        "o "
      else
        "+ "
    }

    println(padding + status(example) + example.description)
    if (stacktrace && example.errors.size > 0) example.errors foreach { printStackTrace(_) }
    
    // if the failure, skip or the error message has linefeeds they must be padded too
    def parens(f: Throwable) = " (" + f.location + ")"
    example.skipped.toList ::: example.failures.toList ::: example.errors.toList foreach { f: Throwable =>
      if (f.getMessage != null)
        println(padding + "  " + f.getMessage.replaceAll("\n", "\n" + padding + "  ") + parens(f)) 
      else
        println(padding + "  exception message is null" + parens(f)) 
    }
  }
}

/**
 * Implementation of the <code>OutputReporter</code> with a <code>ConsoleOutput</code>
 * and a <code>SimpleTimer</code>
 */  
trait Console extends OutputReporter with ConsoleOutput {
  /** this timer uses java Calendar to compute hours, minutes, seconds and milliseconds */
  val timer = new org.specs.util.SimpleTimer
}

/**
 * This class implements the <code>Console</code> trait and can be initialized with specifications directly<br>
 * Usage: <code>object mySpecRunner extends ConsoleRunner(mySpec1, mySpec2)</code>
 */  
class ConsoleRunner(val specifications: Specification*) extends Console {
  val specs = specifications 
  def this() = this(Nil:_*)
  def ConsoleRunner(specs: List[Specification]) = new ConsoleRunner(specs :_*)
}

/**
 * This class can be used to search for specifications on a given path 
 * and execute them.<br>
 * Usage: <code>object myFileRunner extends SpecsFileRunner(path, pattern)</code><br>
 * Where <code>path</code> is a path to a directory containing scala files (it can be a glob: i.e. "dir/**/*spec.scala")
 * and <code>pattern</code> is a regular expression which is supposed to match an object name extending a Specification
 * class named ".*Spec.*"
 */  
class SpecsFileRunner(path: String, pattern: String) extends SpecsFinder(path, pattern, true) with Console 
