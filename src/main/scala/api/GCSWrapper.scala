package api

import common.UsesEnv

import scala.scalajs.js.annotation.JSExport


import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation._

class Config(@JSExport val projectId: String)



@js.native
trait Bucket extends js.Any {
  val name: String = js.native
  def upload(url: String): js.Promise[Any] = js.native

  /**
    * {destination: String}
    * @param config
    * @return
    */
  def upload(url: String, config: js.Dynamic): js.Promise[Any] = js.native

  def getFiles(): Promise[js.Array[js.Array[js.Any]]] = js.native

  def getFiles(query: js.Any): Promise[js.Array[js.Array[js.Any]]] = js.native
}

object Bucket {
  def query = js.Dynamic
}
@js.native
@JSImport("@google-cloud/storage", JSImport.Default)
class GCStorage(config: Config) extends js.Any {
  val baseUrl: String = js.native
  val projectId: String = js.native
  def bucket(name: String): Bucket = js.native
}




trait MixInGCSBucket extends UsesEnv {
  protected def bucket: Bucket = new GCStorage(new Config(env.gcpProjectId)).bucket(env.gcsBucketName)
}

trait UsesGCSBucket {
  protected def bucket: Bucket
}

@js.native
@JSGlobal
class GCSFile extends js.Any {
  /**
    * {destination: String}
    * @param config
    * @return
    */
  def download(config: js.Dynamic): Promise[js.Array[Any]] = js.native
}