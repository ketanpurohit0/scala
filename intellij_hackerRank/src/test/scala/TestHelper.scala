package com.kkp.Unt

import org.scalatest.funsuite.AnyFunSuite
import org.apache.spark.sql.functions._

import scala.Console.println


class TestHelper extends  AnyFunSuite {
  test("HackerRank:Easy:Hello World N Times") {

    def f(n: Int) = {for (i <- 0 until n) println("Hello World")}
    val n = 6
    f(n)
  }

  test("HackerRank:Easy:Reverse a List") {
    val inList = List[Int](1,2,2,6,7,11)
    val r_inList = Helper.reverse_a_list(inList)
    assert(inList.reverse == r_inList)
  }

  test("HackerRank:Easy:AreaUnderCurveAndVolume") {
    val coefficients = List(1,2,3,4,5)
    val powers = List(6,7,8,9,10)
    val lowerLimit = 1
    val upperLimit = 4

    val x=areas_and_volume_under_a_curve.f(List[Int](5,6),List[Int](2,3),2)
    val area_at_a_point = areas_and_volume_under_a_curve.area(List[Int](5,6),List[Int](2,3),2)
    val area_under_curve = areas_and_volume_under_a_curve.summation(areas_and_volume_under_a_curve.f, upperLimit, lowerLimit, coefficients, powers)
    val volume_under_curve = areas_and_volume_under_a_curve.summation(areas_and_volume_under_a_curve.area, upperLimit, lowerLimit, coefficients, powers)

    println(x, area_at_a_point, area_under_curve, volume_under_curve)
  }
}
