import api.{MixInDataStore, MixInGCSBucket}
import common.{MixInNodeEnv, MixInNodeJS}

import scala.util.{Failure, Success}
//import api.{MixInDataStore, MixInGCSBucket}
//import api.toho.MixInTohoApi
//import cinemaScheduleBatch.TohoCinemaSchedulesCollectorBatch
//
import scalajs.concurrent.JSExecutionContext.Implicits.queue
//import dailyScheduleCollectorKickBatch.{MainBatch => DailyCollectorKickMainBatch}
import gcsToDataStoreBatch.{MainBatch => GcsToDataStoreMainBatch}

//import common.{MixInNodeEnv, MixInNodeJS}
//
//import scala.util.{Failure, Success}

//trait MixInTohoCinemaSchedulesCollectorBatch {
//  val tohoCinemaSchedulesCollectorBatch: TohoCinemaSchedulesCollectorBatch = new TohoCinemaSchedulesCollectorBatch
//    with MixInNodeJS
//    with MixInGCSBucket
//    with MixInTohoApi
//    with MixInNodeEnv
//}
//

//object Main {
//  val mainBatch = new MainBatch with MixInTohoCinemaSchedulesCollectorBatch with MixInNodeEnv
//
//  def main(args: Array[String]): Unit = {
//    mainBatch.main(Array("toho", "009")).onComplete {
//      case Success(_) => println("done")
//      case Failure(t) => throw t
//    }
//  }
//}

//object Main {
//  def main(args: Array[String]): Unit = {
//
//  }
//}

object Main {
  val mainBatch = new GcsToDataStoreMainBatch with MixInNodeJS with MixInNodeEnv with MixInDataStore with MixInGCSBucket

  def main(args: Array[String]): Unit = {
//    mainBatch.execute(Array()).onComplete {
//      case Success(_) => println("done")
//      case Failure(t) => throw t
//    }
  }
}
