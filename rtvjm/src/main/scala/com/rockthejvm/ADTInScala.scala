package com.rockthejvm

object ADTInScala extends App {

    // a way of structuring your data

  // Sum Type
  sealed trait Weather
  case object Sunny extends Weather
  case object Windy extends Weather
  case object Rainy extends Weather
  case object Cloudy extends Weather

  // Weather = {Sunny + Windy + Rainy + Cloudy } == Sum Type
  // All possible cases are enumerated

  def feeling(weather: Weather): String = weather match {
    case Sunny =>":)"
    case Windy =>":|"
    case Rainy =>":("
    case Cloudy =>":CC"
  }
  // Product Type
  case class WeatherForecast(lat: Double, long: Double)
  // (Double, Double) => WeatherForecast
  // ie Product Type

  // hybrid type
  sealed trait WeatherForecastResponse //SumType
  case class Valid(weather: Weather) extends WeatherForecastResponse
  case class Invalid (error: String, descr: String) extends WeatherForecastResponse

  // why
  // illegal states are not representable
  // composable
  // ADT only store data => immutable data structures
  // code structure

  sealed trait WeatherServerError
  case object NotAvailable extends WeatherServerError
}
