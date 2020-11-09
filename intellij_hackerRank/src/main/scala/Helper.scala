package com.kkp.Unt
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import java.sql.Connection
import java.sql.{DriverManager, ResultSet}
import java.sql.Driver

import scala.collection.mutable.ListBuffer
object Helper {

  // HackerRank:Easy:Reverse a List
  // https://www.hackerrank.com/challenges/fp-reverse-a-list/problem
  def reverse_a_list(arr: List[Int]): List[Int] = {
    var reversed_list = ListBuffer[Int]()
    for (idx <- arr.length - 1 to 0 by -1) {
      reversed_list += arr(idx)
    }
    return reversed_list.toList
  }
}

  // HackerRank:Easy:AreaUnderCurveAndVolume
  //https://www.hackerrank.com/challenges/area-under-curves-and-volume-of-revolving-a-curv/problem

object areas_and_volume_under_a_curve  {
    import scala.math.pow
    def f(coefficients:List[Int],powers:List[Int],x:Double):Double =
    {
      //Fill Up this function body
      // To compute the value of the function
      // For the given coefficients, powers and value of x
      var sum = 0D
      for (index <- 0 to coefficients.length-1){
        sum+= coefficients(index)*(pow(x,powers(index)))
      }
      return sum
    }

    // This function will be used while invoking "Summation" to compute
    // The Volume of revolution of the curve around the X-Axis
    // The 'Area' referred to here is the area of the circle obtained
    // By rotating the point on the curve (x,f(x)) around the X-Axis
    def area(coefficients:List[Int],powers:List[Int],x:Double):Double =
    {
      //Fill Up this function body
      // To compute the area of the circle on revolving the point
      // (x,f(x)) around the X-Axis
      // For the given coefficients, powers and value of x
      return scala.math.Pi * pow(f(coefficients, powers, x),2)
    }

    // This is the part where the series is summed up
    // This function is invoked once with func = f to compute the area 	     // under the curve
    // Then it is invoked again with func = area to compute the volume
    // of revolution of the curve
    def summation(func:(List[Int],List[Int],Double)=>Double,upperLimit:Int,lowerLimit:Int,coefficients:List[Int],powers:List[Int]):Double =
    {
      // Fill up this function
      var sum = 0D
      val interval = 0.001D
      for (x <- BigDecimal(lowerLimit.toDouble) to BigDecimal(upperLimit.toDouble) by interval) {
        sum+= func(coefficients, powers, x.toDouble)
      }
      return sum*interval
    }
  }

