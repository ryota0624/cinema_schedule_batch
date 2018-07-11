package gcsToDataStoreBatch

import java.time

import api.{Bucket, UsesDataStore, UsesGCSBucket}
import common.{Batch, Date, UsesNodeJS}
import gcsToDataStoreBatch.entity.{TohoCinemaReadEntity, TohoCinemaSchedule}
import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.npm.readablestream.Readable

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.{Promise, |}
import scala.scalajs.js.annotation.JSGlobal
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import io.scalajs.npm.csvparse._


@JSGlobal
@js.native
class File extends js.Any {
  val name: String = js.native
  def download(options: js.Dynamic): Promise[Buffer] = js.native
}

trait MainBatch extends UsesDataStore with UsesGCSBucket with Batch with UsesNodeJS {
  import api.ExtendDataStoreOps._
  import api.ExtendDataStore._

  override def execute(args: Array[String])(implicit ctx: ExecutionContext): Future[Unit] = {
    implicit val date: time.LocalDate = args.headOption.map(Date.parse).getOrElse{
      console.log("no args start")
      time.LocalDate.now()
    }

    console.log(s"start batch: date args = ${Date.format(date)}")

    for {
       _ <- resetDataStore()
      files <- getFiles()
      entities <- csvFilesToEntities(files)
      result <- dataStore.saveEntities(entities).toFuture
    } yield {
      console.log(s"insarted entities count: ${entities.size}")
      result
    }
  }

  private def getFiles()(implicit date: time.LocalDate): Future[Seq[String]] = {
    bucket.getFiles(Bucket.query.literal(
      prefix = Date.format(date),
      suffix = ".csv"
    )).toFuture.flatMap { (result) =>
      val files = result(0).map(_.asInstanceOf[File])
      console.log(s"downloaded files ${files.map(_.name).mkString(",")}")
      Future.sequence(files.map(_.download(js.Dynamic.literal()).toFuture.map(_.toString("utf-8"))).toSeq)
    }
  }

  private def resetDataStore()(implicit date: time.LocalDate): Future[Any] = {
    console.log(s"resetDataStore namespace: ${Date.format(date)}, kind: ${TohoCinemaSchedule.kind}")
    val query = dataStore.createQuery(Date.format(date), TohoCinemaSchedule.kind)
    dataStore.runQuery[TohoCinemaReadEntity](query)
      .toFuture.map(_.head).map { schedules =>
      schedules.map({ s =>s(dataStore.KEY)})
    }.map(keys => dataStore.delete(keys))
  }

  private def csvFilesToEntities(csvFiles: Seq[String])(implicit date: java.time.LocalDate): Future[Seq[TohoCinemaSchedule]] = {
    Future.sequence(csvFiles.map(csvToEntities(Date.format(date), _))).map(_.flatten)
  }

  private def csvToEntities(date: String, csv: String): Future[Seq[TohoCinemaSchedule]] = {
    val promise = scala.concurrent.Promise.apply[Seq[TohoCinemaSchedule]]()
    val parser = CsvParse(new ParserOptions(
      comment = "#",
      auto_parse = true,
      columns = true,
      delimiter = ",",
      quote = "\"",
      relax = true,
      rowDelimiter = "\n",
      skip_empty_lines = true,
      trim = true
    ))

    val array = js.Array[TohoCinemaSchedule]()

    val readable = new Readable()

    parser.onData[js.Dictionary[String | Int]]({ data =>
      array.push(TohoCinemaSchedule(date, data))
    })

    readable._read = () => {}
    readable.push(csv)
    readable.push(null)
    readable.pipe(parser)

    readable.onEnd(() => {
      promise.success(array)
    })

    promise.future
  }
}
