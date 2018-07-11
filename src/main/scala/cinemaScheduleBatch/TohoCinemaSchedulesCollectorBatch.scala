package cinemaScheduleBatch

import java.time

import api.toho._
import api.UsesGCSBucket
import common.{Batch, CsvHeader, Date, UsesNodeJS}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON

trait TohoCinemaSchedulesCollectorBatch extends Batch with UsesTohoApi with UsesGCSBucket with UsesNodeJS {
  implicit val cinemaToCsv: ConvertCsv[Cinema] = new ConvertCsv[Cinema] {
    override def run(cinema: Cinema): Seq[String] = {
      Seq(CsvHeader.tohoCinemaScheduleHeader.mkString(",")) ++ cinema.movies.flatMap({ movie =>
        ConvertCsv.toCsv(movie).map(movieCsvLine => s"${cinema.name},$movieCsvLine")
      })
    }
  }

  implicit val movieToCsv: ConvertCsv[Movie] = new ConvertCsv[Movie] {
    override def run(e: Movie): Seq[String] = {
      e.screens.flatMap({ screen =>
        ConvertCsv.toCsv(screen).map(screenCsvLine => s"${e.name}, ${e.hours}, $screenCsvLine")
      })
    }
  }

  implicit val screenToCsv: ConvertCsv[Screen] = new ConvertCsv[Screen] {
    override def run(e: Screen): Seq[String] = {
      e.plans.filterNot(plan => plan.showingStart.isEmpty && plan.showingEnd.isEmpty)
        .flatMap({ plan =>
          ConvertCsv.toCsv(plan).map(planCsvLine => s"${e.name}, $planCsvLine")
        })
    }
  }

  implicit val planToCsv: ConvertCsv[Plan] = new ConvertCsv[Plan] {
    override def run(e: Plan): Seq[String] = Seq(s"${e.showingStart}, ${e.showingEnd}")
  }

  def execute(args: Array[String])(implicit ctx: ExecutionContext): Future[Unit] = Future {
    console.log("start toho cinema collector batch")
    implicit val (cinemaId , date)  = extractArgs(args)
    val executionResult = for {
      cinemaSchedules <- requestTohoApi(cinemaId).map(_.data.pop().cinemas.pop())
      filePath <- cinemaSchedulesWriteTmpFile(cinemaSchedules)
      result <- uploadTohoResponseFileToBucket(cinemaId, filePath)
    } yield result

    executionResult map { _ =>
      console.info("toho cinema collector batch success")
    }
  }

  def extractArgs(args: Array[String]): (String, time.LocalDate) = args.headOption.getOrElse {
    throw new Error("invalid args")
  } -> args.slice(1, 2).headOption.map(Date.parse).getOrElse(time.LocalDate.now())

  def requestTohoApi(cinemaId: String)(implicit ctx: ExecutionContext, date: time.LocalDate): Future[TohoResponse] = {
    console.log(s"start request toho cinema ${cinemaId}")
    tohoApi.execute(cinemaId, date).map { responseString =>
      val response = TohoResponse(JSON.parse(responseString))
      console.log(s"finish request toho cinema ${cinemaId}")
      response
    }
  }

  def cinemaSchedulesWriteTmpFile(cinema: Cinema)(implicit ctx: ExecutionContext): Future[String] = {
    console.log(s"start cinemaSchedulesWriteTmpFile")

    val tmpFilePath = s"${os.tmpdir()}/collector-batch-toho-cinema-${cinema.name}.csv"
    fs.writeFileFuture(tmpFilePath, ConvertCsv.toCsv(cinema).mkString("\n"))
      .map { _ =>
        console.log(s"finish cinemaSchedulesWriteTmpFile")
        tmpFilePath
      }
  }

  def uploadTohoResponseFileToBucket(cinemaId: String, tmpFilePath: String)(implicit ctx: ExecutionContext, date: time.LocalDate): Future[Any] = {
    val timeStamp = Date.format(date)

    val fileName = tmpFilePath.split("/").reverse.head

    console.log(s"upload File -> ${s"${timeStamp}/${cinemaId}-${fileName}"}")

    bucket.upload(tmpFilePath, js.Dynamic.literal(destination = s"${timeStamp}/${fileName}")).toFuture
  }
}


trait UsesTohoCinemaSchedulesCollectorBatch {
  protected val tohoCinemaSchedulesCollectorBatch: TohoCinemaSchedulesCollectorBatch
}
