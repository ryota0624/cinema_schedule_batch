package api

import common.UsesEnv

import scala.scalajs.js
import scala.scalajs.js.Promise
import scala.scalajs.js.annotation.{JSGlobal, JSImport}

trait ExtendDataStore[D <: DataStore] {
  protected def entity[E <: DataStoreEntity](e: E): js.Any = js.Dynamic.literal(data = e.toDto, key = e.key.toJS)

  def save[E <: DataStoreEntity](dataStore: D, e: E): Promise[js.Any]
  def insert[E <: DataStoreEntity](dataStore: D, entities: Seq[E]): Promise[js.Any]
  def delete(dataStore: D, key: DataStoreKey): Promise[js.Any]
}

object ExtendDataStore {
  implicit def dataStoreExtend: ExtendDataStore[DataStore] = new ExtendDataStore[DataStore] {
    override def save[E <: DataStoreEntity](dataStore: DataStore, e: E): Promise[js.Any] = dataStore.save(entity(e))

    override def delete(dataStore: DataStore, key: DataStoreKey): Promise[js.Any] = dataStore.delete(key.toJS)
    override def insert[E <: DataStoreEntity](dataStore: DataStore, entities: Seq[E]): Promise[js.Any] = dataStore.insert(js.Array(entities.map(entity): _*))
  }
}

trait ExtendDataStoreOps[T <: DataStore] {
  def self: T

  implicit def instance: ExtendDataStore[T]

  def deleteByKey(key: DataStoreKey): Promise[js.Any] = instance.delete(self, key)
  def saveEntity[E <: DataStoreEntity](e: E): Promise[js.Any] = instance.save(self,  e)
  def saveEntities[E <: DataStoreEntity](entities: Seq[E]): Promise[js.Any] = instance.insert(self, entities)
}

object ExtendDataStoreOps{
  implicit def extendDataStoreOps[D <: DataStore](dataStore: D)(implicit exDataStore: ExtendDataStore[D]): ExtendDataStoreOps[D] =
    new ExtendDataStoreOps[D] {
      override def self: D = dataStore
      override implicit def instance: ExtendDataStore[D] = exDataStore
    }
}

object DataStore {
  type Kind = String
  type Namespace = String
}

@JSGlobal
@js.native
class RunQueryResult extends js.Any {
  val moreResults:String = js.native
  val endCursor: String = js.native
}

@JSGlobal
@js.native
class DataStoreQuery extends js.Any

@js.native
@JSImport("@google-cloud/datastore", JSImport.Default)
class DataStore(config: Config) extends js.Any {
  def key(props: js.Array[String]): js.Any = js.native
  def save(e: js.Any): Promise[js.Any] = js.native
  def insert(entities: js.Array[js.Any]): Promise[js.Any] = js.native

  val KEY: js.Symbol = js.native
  def delete(): Promise[js.Any] = js.native
  def delete(e: js.Any): Promise[js.Any] = js.native
  def createQuery(nameSpace: String ,kind: String): DataStoreQuery = js.native
  def runQuery[E](query: DataStoreQuery): Promise[js.Array[js.Array[E]]] = js.native
}



trait DataStoreEntity {
  val key: DataStoreKey
  def toDto: js.Dynamic
}

case class DataStoreKey(kind: api.DataStore.Kind, namespace: api.DataStore.Namespace) {
  def toJS: js.Any = js.Dynamic.literal(namespace = namespace, kind = kind)
}

trait UsesDataStore extends {
  protected def dataStore: DataStore
}

trait MixInDataStore extends UsesEnv {
  protected def dataStore: DataStore = new DataStore(new Config(env.gcpProjectId))
}
