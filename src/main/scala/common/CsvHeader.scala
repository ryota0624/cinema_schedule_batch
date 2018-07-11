package common

object CsvHeader {
  val movieName = "映画名"
  val theaterName = "映画館名"
  val showTime = "上映時間/min"
  val screenName = "スクリーン名"
  val startTime = "上映開始時間"
  val endTime = "上映終了時間"
  val tohoCinemaScheduleHeader = Seq(theaterName, movieName, showTime, screenName, startTime, endTime)
}
