package api.toho

import java.time

import api.Request
import common.{Env, UsesEnv}
import io.scalajs.nodejs.console

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobal, JSName}
import scala.scalajs.js.UndefOr

trait TohoApi {
  def execute(id: String, date: time.LocalDate)(implicit ctx: ExecutionContext): Future[String]
}

class TohoApiImpl(env: Env) extends TohoApi {
  private lazy val endPoint: String = env.tohoUrl

  private def url(placeId: String, showDate: time.LocalDate): String = {
    val month = if (showDate.getMonthValue < 10) s"0${showDate.getMonthValue}" else showDate.getMonthValue
    val day = if (showDate.getDayOfMonth < 10) s"0${showDate.getDayOfMonth}" else showDate.getDayOfMonth
    s"$endPoint?vg_cd=${placeId}&show_day=${showDate.getYear}${month}${day}&term=99&__type__=json"
  }

  def execute(id: String, date: time.LocalDate)(implicit ctx: ExecutionContext): Future[String] = {
    console.log(url(id, date))
    Request.getFuture(url(id, date)).map(buffer => buffer.toString("utf-8"))
  }
}

trait UsesTohoApi {
  protected val tohoApi: TohoApi
}

trait MixInTohoApi extends UsesEnv {
  protected lazy val tohoApi: TohoApi = new TohoApiImpl(env)
}

object TohoApi {
  private val locationSeq = Seq(
    ("009", "roppongi"),
    ("043", "sibuya"),
    ("076", "sinjuku")
  )
}

sealed case class SeatStatus(value: String)


@JSGlobal
@js.native
class UnsoldSeatInfo extends js.Any {
  val unsoldSeatStatus: UndefOr[String] = js.native
}

@JSGlobal
@js.native
class Plan extends js.Any {
  val showingStart: String = js.native
  val showingEnd: String = js.native
  val unsoldSeatInfo: UnsoldSeatInfo = js.native
}

@JSGlobal
@js.native
class Screen extends js.Any {
  val name: String = js.native
  @JSName("list")
  val plans: js.Array[Plan] = js.native
}

@JSGlobal
@js.native
class Movie extends js.Any {
  val hours: Int = js.native
  val name: String = js.native
  @JSName("list")
  val screens: js.Array[Screen] = js.native
}

@JSGlobal
@js.native
class Cinema extends js.Any {
  val name: String = js.native
  @JSName("list")
  val movies: js.Array[Movie] = js.native
}

object TohoResponse {
  def apply(json: scalajs.js.Dynamic): TohoResponse = {
    json.asInstanceOf[TohoResponse]
  }
}

@JSGlobal
@js.native
class TohoResponse extends js.Any {
  val data: js.Array[Toho] = js.native
  val status: String = js.native
}

@JSGlobal
@js.native
class Toho extends js.Any {
  @JSName("list")
  val cinemas: js.Array[Cinema] = js.native
}