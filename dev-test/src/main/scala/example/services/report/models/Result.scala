package example.services.report.models

case class Result(
  labels: List[String],
  responses: List[Int],
  results: List[Double],
  average: Option[Double],
)