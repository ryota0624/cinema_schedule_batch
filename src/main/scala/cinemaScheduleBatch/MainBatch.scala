package cinemaScheduleBatch

import common.{Batch, UsesEnv}

import scala.concurrent.{ExecutionContext, Future}

trait MainBatch extends UsesTohoCinemaSchedulesCollectorBatch with UsesEnv {
  def main(args: Array[String])(implicit ctx: ExecutionContext): Future[Unit] = {
    findExecuteBatch(args.head).execute(args.tail)
  }

  def findExecuteBatch(batchName: String): Batch = {
    batchName match {
      case "toho" => tohoCinemaSchedulesCollectorBatch
      case _ => throw new Error(s"not found batch ${batchName}")
    }
  }
}