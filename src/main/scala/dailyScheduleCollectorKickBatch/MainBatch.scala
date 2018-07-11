package dailyScheduleCollectorKickBatch

import java.time

import io.scalajs.npm.request._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Success
import common._
import api.UsesDataStore

import scala.scalajs.js

trait MainBatch extends UsesEnv with Batch with UsesDataStore with UsesNodeJS {
  override def execute(args: Array[String])(implicit ctx: ExecutionContext): Future[Unit] = {
    val date = args.headOption.map(Date.parse).getOrElse(time.LocalDate.now())

    console.log(s"batch execute date: ${Date.format(date)}")

    kickTohoCinemaBatch(date)
  }

  def kickTohoCinemaBatch(date: time.LocalDate)(implicit ctx: ExecutionContext): Future[Unit] = {
    console.log("start kickTohoCinemaBatch")
    val query = dataStore.createQuery("toho_cinema_batch", "TohoCinemaBatch")
    dataStore.runQuery[TohoCinemaEntity](query).toFuture map { result =>
      val cinemas = result.head
      cinemas.foreach(cinema => console.log(s"kick target: ${cinema.name}"))
      val kickBatchs: Seq[Future[Unit]] = cinemas.map({ cinema =>
        console.log(s"kick batch ${cinema.id}:${cinema.name}")
        val body = js.Dynamic.literal(args = s"toho,${cinema.id},${Date.format(date)}")
        val requestOption: RequestOptions = new RequestOptions(json = body, url = env.cinemaScheduleCollectorBatchPath)
        Request.postFuture(requestOption).flatMap({ case (message, requestBody) =>
          val promise = Promise[Unit]()
          console.log(s"statusCode: ${message.statusCode}")
          if (message.statusCode != 200) {
            throw new Error(s"fail kick toho batch ${message.statusMessage}")
          }

          message.onEnd {() =>
            console.log(s"${message.trailers.mkString("\n")}")
            promise.complete(Success())
          }

          message.onError{ error =>
            console.error(error.message, error.stack)
            promise.failure(error)
          }

          promise.future
        })
      }).toSeq

      Future.sequence(kickBatchs)
    }
  }
}
