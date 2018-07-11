package gcsToDataStoreBatch.entity

import api.{DataStoreEntity, DataStoreKey}
import common.CsvHeader

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSBracketAccess, JSGlobal, JSImport, JSName}
import scala.scalajs.js.|

object TohoCinemaSchedule {
  val kind = "TohoCinemaSchedule"

  def apply(date: String, dic: js.Dictionary[String | Int]): TohoCinemaSchedule = {
    TohoCinemaSchedule(
      date = date,
      theaterName = dic.get(CsvHeader.theaterName).get.asInstanceOf[String],
      movieName = dic.get(CsvHeader.movieName).get.asInstanceOf[String],
      showTime = dic.get(CsvHeader.showTime).get.asInstanceOf[Int],
      startTime = dic.get(CsvHeader.startTime).get.asInstanceOf[String],
      endTime = dic.get(CsvHeader.endTime).get.asInstanceOf[String]
    )
  }
}

case class TohoCinemaSchedule(
                               theaterName: String,
                               movieName: String,
                               showTime: Int,
                               startTime: String,
                               endTime: String,
                               date: String) extends DataStoreEntity {
  override val key: DataStoreKey = DataStoreKey(TohoCinemaSchedule.kind, date)

  override def toDto: js.Dynamic = js.Dynamic.literal(
    theaterName = theaterName,
    movieName = movieName,
    showTime = showTime,
    startTime = startTime,
    endTime = endTime
  )
}
@JSGlobal
@js.native
class TohoCinemaReadEntity extends js.Any {
  @JSBracketAccess
  def apply(index: js.Symbol): js.Any = js.native
  val KEY_SYMBOL: js.Any = js.native
  val theaterName: String = js.native
  val movieName: String = js.native
  val showTime: Int = js.native
  val startTime: String = js.native
  val endTime: String = js.native
}