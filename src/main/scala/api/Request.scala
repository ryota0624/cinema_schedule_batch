package api

import io.scalajs.nodejs.buffer.Buffer
import io.scalajs.nodejs.console
import io.scalajs.nodejs.https.Https
import io.scalajs.nodejs.http.{Http, RequestOptions}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

object Request {
  def request(options: RequestOptions)(implicit executionContext: ExecutionContext): Future[Buffer] = {
    console.log(s"requestFuture: start request ${options.path}")

    val responseFuture = if (options.protocol.map(_.matches(".*https.*")).getOrElse(true)) {
      Https.requestFuture(options)
    } else {
      Http.requestFuture(options)
    }

    responseFuture.flatMap[Buffer] { response =>
      var chunkedBuffer: Buffer = null
      val promise = Promise.apply[Buffer]()

      response.onData({ buffer =>
        if (chunkedBuffer == null) {
          chunkedBuffer = buffer
        } else {
          chunkedBuffer = Buffer.concat(scala.scalajs.js.Array(chunkedBuffer, buffer))
        }
      })

      response.onEnd({ () =>
        console.log(s"requestFuture: finish request ${options.path}")
        promise.tryComplete(Success(chunkedBuffer))
      })

      promise.future
    }
  }
  def getFuture(url: String)(implicit executionContext: ExecutionContext): Future[Buffer] = {
    console.log(s"getFuture: start request ${url}")

    val responseFuture = if (url.matches(".*https.*")) {
      Https.getFuture(url)
    } else {
      Http.getFuture(url)
    }

    responseFuture.flatMap[Buffer] { response =>
      var chunkedBuffer: Buffer = null
      val promise = Promise.apply[Buffer]()

      response.onData({ buffer =>
        if (chunkedBuffer == null) {
          chunkedBuffer = buffer
        } else {
          chunkedBuffer = Buffer.concat(scala.scalajs.js.Array(chunkedBuffer, buffer))
        }
      })

      response.onEnd({ () =>
        console.log(s"getFuture: finish request ${url}")
        promise.tryComplete(Success(chunkedBuffer))
      })

      promise.future
    }

  }
}
