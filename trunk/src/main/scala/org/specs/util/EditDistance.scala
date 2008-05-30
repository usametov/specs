package org.specs.util

object EditDistance extends EditDistance
trait EditDistance {
  case class EditMatrix(s1: String, s2: String) {
    val matrix = new Array[Array[int]](s1.length, s2.length)
    for (i <- 0 to s1.length - 1;
         j <- 0 to s2.length - 1) {
      if (i == 0) matrix(i)(j) = j // j insertions
      else if (j == 0) matrix(i)(j) = i  // i suppressions
      else matrix(i)(j) = min(matrix(i - 1)(j) + 1, // suppression
                              matrix(i - 1)(j - 1) + (if (s1(i) == s2(j)) 0 else 1), // substitution
                              matrix(i)(j - 1) + 1) // insertion
    
    }
    def distance = matrix(s1.length - 1)(s2.length - 1) 
    def print = { 
      for (i <- 0 to s1.length - 1) {
        def row = for (j <- 0 to s2.length - 1) yield matrix(i)(j)
        println(row.mkString("|"))
      }
      this
    }
    def operations = {
	  def modify(s: String, c: Char) = ("(" + c.toString + ")" + s).replaceAll("\\)\\(", "")
      def findOperations(dist: Int, i: Int, j:Int, s1mod: String, s2mod: String): (String, String) = {
        if (i + j == 0) {
  	      if (dist == 0) (s1(0) + s1mod, s2(0) + s2mod)
          else (modify(s1mod, s1(0)), modify(s2mod, s2(0))) 
        }
        else {
	      val (suppr, subst, ins) = (matrix(i - 1)(j), matrix(i - 1)(j - 1), matrix(i)(j - 1))   
	      if (suppr < subst) 
	        findOperations(suppr, i - 1, j, modify(s1mod, s1(i)), s2mod)
	      else if (ins < subst)
	        findOperations(ins, i, j - 1, s1mod, modify(s2mod, s2(j)))
	      else if (subst < dist)
	        findOperations(subst, i - 1, j - 1, modify(s1mod, s1(i)), modify(s2mod, s2(j)))
	      else
	        findOperations(subst, i - 1, j - 1, s1(i) + s1mod, s2(j) + s2mod)
	    }
      }
      findOperations(distance, s1.length - 1, s2.length - 1, "", "")
    }
    def min(suppr: Int, subst: Int, ins: =>Int) = {
      if(suppr < subst) suppr
      else if (ins < subst) ins
      else subst
    }
  }
  def editDistance(s1: String, s2: String): Int = EditMatrix(s1, s2).distance
  def showMatrix(s1: String, s2: String) = EditMatrix(s1, s2).print
  def showDistance(s1: String, s2: String) = {
    EditMatrix(s1, s2).operations
  }
}