package common

import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.os.OS
import io.scalajs.nodejs.{Console, process}


trait UsesEnv {
  protected val env: Env
}

trait MixInNodeEnv {
  protected val env: Env = NodeEnv
}

trait UsesNodeJS {
  protected val os: OS
  protected val fs: Fs
  protected val console: Console
}

trait MixInNodeJS {
  protected val os: OS = OS
  protected val fs: Fs = Fs
  protected val console: Console = io.scalajs.nodejs.console
}

trait Env {
  val tohoUrl: String
  val gcpProjectId: String
  val gcsBucketName: String
  val cinemaScheduleCollectorBatchPath: String
  val gcsToDataStoreBatchPath: String
}

object NodeEnv extends Env {
  lazy val tohoUrl: String = process.env("TOHO_ENDPOINT_URL")
  lazy val gcpProjectId: String = process.env("GCP_PROJECT_ID")
  lazy val gcsBucketName: String = process.env("GCS_BUCKET_NAME")
  lazy val cinemaScheduleCollectorBatchPath: String = process.env("CINEMA_SCHEDULE_COLLECTOR_BATCH_PATH")
  lazy val gcsToDataStoreBatchPath: String = process.env("GCS_TO_DATA_STORE_BATCH_PATH")
}
