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
    import scala.math.abs
    val tests = Seq[(List[Int], List[Int], Int, Int, Double, Double)](
      (List(1, 1), List(2, 3), 5, 6, (2377D / 12), 4210921D * scala.math.Pi / 105),
      (List(1,1,4,5),List(2,3,0,4),5,6,4853.08333,77115750.08501),
      (List(1,2,3,4,5),List(6,7,8,9,10),1,4,2435300.3,26172951168940.8),
      (List(1,2), List(0,1),2,20,414,36024.1),
      (List(1,2,3,4,5,6,7,8),List(-1,-2,-3,-4,1,2,3,4),1,2,101.4,41193.0),
      (List(-1, 2, 0, 2, -1, -3, -4, -1, -3, -4, -999, 1, 2, 3, 4, 5, 1, 2, 0, 2, -1, -3, -4, -1, -3, -4, -999, 1, 2, 3, 4, 5),
        List(-16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),1,2,-152853.7,196838966733.0)
    )

    tests.foreach(x => {
      val coefficients = x._1
      val powers  = x._2
      val lowerLimit = x._3
      val upperLimit = x._4
      val expectedArea = x._5
      val expectedVol = x._6
      val area_under_curve = areas_and_volume_under_a_curve.summation(areas_and_volume_under_a_curve.f, upperLimit, lowerLimit, coefficients, powers)
      val volume_under_curve = areas_and_volume_under_a_curve.summation(areas_and_volume_under_a_curve.area, upperLimit, lowerLimit, coefficients, powers)
      println("**")
      println(area_under_curve, expectedArea)
      println(volume_under_curve, expectedVol)
      //assert(abs(area_under_curve-expectedArea) < 1)
      //assert(abs(volume_under_curve-expectedVol) < 1)

    })
  }



}
