package common

import scala.concurrent.{ExecutionContext, Future}

trait Batch {
  def execute(args: Array[String])(implicit ctx: ExecutionContext): Future[Unit]
}