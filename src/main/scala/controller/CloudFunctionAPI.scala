package controller

import api.{DataStore, MixInDataStore, MixInGCSBucket}
import api.toho.MixInTohoApi
import cinemaScheduleBatch.{TohoCinemaSchedulesCollectorBatch, MainBatch => CinemaScheduleMainBatch}
import dailyScheduleCollectorKickBatch.{MainBatch => DailyScheduleCollectorKickMainBatch}
import gcsToDataStoreBatch.{MainBatch => GcsToDataStoreBatch, StarterBatch => GcsToDataStoreBatchStarter}
import common.{Env, MixInNodeJS}
import io.scalajs.nodejs.{Console, console}
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.os.OS
import io.scalajs.npm.express.{Request, Response}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel, JSGlobal, JSImport}
import scala.util.{Failure, Success}
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.UndefOr

@js.native
@JSImport("./env.json", JSImport.Default)
object ENV extends js.Any {
  val TOHO_ENDPOINT_URL: String = js.native
  val GCP_PROJECT_ID: String = js.native
  val GCS_BUCKET_NAME: String = js.native
  val CINEMA_SCHEDULE_COLLECTOR_BATCH_PATH: String = js.native
  val GCS_TO_DATA_STORE_BATCH_PATH: String = js.native
}

object CloudFunctionEnv extends Env {
  lazy val gcpProjectId: String = ENV.GCP_PROJECT_ID
  lazy val gcsBucketName: String = ENV.GCS_BUCKET_NAME
  lazy val tohoUrl: String = ENV.TOHO_ENDPOINT_URL
  lazy val cinemaScheduleCollectorBatchPath: String = ENV.CINEMA_SCHEDULE_COLLECTOR_BATCH_PATH
  lazy val gcsToDataStoreBatchPath: String = ENV.GCS_TO_DATA_STORE_BATCH_PATH
}

trait MixInCloudFunctionEnv {
  val env = CloudFunctionEnv
}

@JSExportTopLevel("CloudFunctionAPI")
object CloudFunctionAPI extends CloudFunctionAPI() {
  @JSExportTopLevel("cinemaSchedulesCollector")
  def runCollectorBatch(req: Request, res: Response) = handleRequest(req, res)

  @JSExportTopLevel("cinemaScheduleBatchKicker")
  def runKicker(req: Request, res: Response) = handleKickerRequest(req, res)

  @JSExportTopLevel("gcsToDataStore")
  def gcsToDataStore(req: Request, res: Response) = handleGcsToDataStoreRequest(req, res)

  @JSExportTopLevel("cinemaScheduleWatcher")
  def watchCinemaScheduleCsvUpload(event: Event) = handleCinemaScheduleCsvUpload(event)
}

@JSGlobal
@js.native
class RequestBody extends js.Any {
  val args: UndefOr[String] = js.native
}

trait MixInTohoCinemaSchedulesCollectorBatch {
  val tohoCinemaSchedulesCollectorBatch: TohoCinemaSchedulesCollectorBatch = new TohoCinemaSchedulesCollectorBatch with MixInNodeJS with MixInCloudFunctionEnv with MixInGCSBucket with MixInTohoApi
}

class CloudFunctionAPI {
  def handleRequest(req: Request, res: Response): Unit = {
    val body = req.body.asInstanceOf[RequestBody]

    if (body.args == js.undefined) {
      res.status(400)
      res.send(null)

    } else {
      val args = body.args.get
      val mainBatch = new CinemaScheduleMainBatch with MixInCloudFunctionEnv with MixInTohoCinemaSchedulesCollectorBatch

      mainBatch.main(args.split(",")) onComplete {
        case Success(_) =>
          console.log("success handle request")
          res.send()
        case Failure(t) =>
          console.error(t.getMessage, t.getStackTrace)
          res.status(500)
          res.send(null)
      }
    }
  }

  def handleKickerRequest(req: Request, res: Response): Unit = {
    val body = req.body.asInstanceOf[RequestBody]

    val mainBatch = new DailyScheduleCollectorKickMainBatch with MixInDataStore with MixInNodeJS with MixInCloudFunctionEnv

    if (body.args == js.undefined) {
      res.status(400)
      res.send(null)

    } else {
      mainBatch.execute(body.args.get.split(",")) onComplete {
        case Success(_) =>
          console.log("success handle request")
          res.send()
        case Failure(t) =>
          console.error(t.getMessage, t.getStackTrace)
          res.status(500)
          res.send(null)
      }
    }
  }

  def handleGcsToDataStoreRequest(req: Request, res: Response): Unit = {
    val body = req.body.asInstanceOf[RequestBody]

    if (body.args == js.undefined) {
      res.status(400)
      res.send(null)
    } else {
      val args = body.args.get
      val mainBatch = new GcsToDataStoreBatch with MixInDataStore with MixInNodeJS with MixInCloudFunctionEnv with MixInGCSBucket
      mainBatch.execute(args.split(",")) onComplete {
        case Success(_) =>
          console.log("success handle request")
          res.send()
        case Failure(t) =>
          console.error(t.getMessage, t.getStackTrace)
          res.status(500)
          res.send(null)
      }
    }
  }

  def handleCinemaScheduleCsvUpload(event: Event): Unit = {
    val batch = new GcsToDataStoreBatchStarter with MixInGCSBucket with MixInCloudFunctionEnv with MixInDataStore
    batch.execute(event) onComplete {
      case Success(_) =>
        console.log("success handle request")
      case Failure(t) =>
        console.error(t.getMessage, t.getStackTrace)
    }
  }
}