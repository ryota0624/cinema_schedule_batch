package gcsToDataStoreBatch

import api.{Bucket, UsesDataStore, UsesGCSBucket}
import common.{Date, UsesEnv}
import controller.Event
import dailyScheduleCollectorKickBatch.TohoCinemaEntity
import io.scalajs.npm.request.{Request, RequestOptions}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

trait StarterBatch extends UsesDataStore with UsesGCSBucket with UsesEnv {

  case class CinemaScheduleFile(name: String) {
    val filePath: Seq[String] = name.split("/")
    val date: String = filePath.head
    val fileName: String = filePath.reverse.head
    val cinemaId: String = fileName.split("-").reverse.head
  }

  def execute(event: Event): Future[Unit] = {
    if (event.contentType.matches(".*csv.*") && event.name.matches(".*collector-batch-toho-cinema.*")) {
      Future {
        val receiveScheduleFile = CinemaScheduleFile(event.name)

        val year = Date.parse(receiveScheduleFile.date)

        val query = dataStore.createQuery("toho_cinema_batch", "TohoCinemaBatch")

        val cinemaIdsFromDataStoreF: Future[Seq[String]] = dataStore.runQuery[TohoCinemaEntity](query).toFuture.map { results =>
          val cinemas = results.head
          cinemas.map(_.id)
        }

        val cinemaIdsFromGCSF: Future[Seq[String]] = bucket.getFiles(Bucket.query.literal(
          prefix = receiveScheduleFile.date,
          suffix = ".csv"
        )).toFuture.map { (result) =>
          result(0).map(_.asInstanceOf[File]).map(file => CinemaScheduleFile(file.name).cinemaId)
        }

        Future.sequence(cinemaIdsFromDataStoreF :: cinemaIdsFromGCSF :: Nil).map {
          case List(fromDataStore, fromGCS, _) =>
            fromDataStore.toSet == fromGCS.toSet
        } map { allCsvFileReadly =>
            if (allCsvFileReadly) {
              val body = js.Dynamic.literal(args = s"${receiveScheduleFile.date}")
              val requestOption: RequestOptions = new RequestOptions(json = body, url = env.gcsToDataStoreBatchPath)
              Request.postFuture(requestOption)
            } else {
              ()
          }
        }
      }
    } else {
      Future()
    }
  }
}
