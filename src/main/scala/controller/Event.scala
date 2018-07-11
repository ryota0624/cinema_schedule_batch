package controller

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
object Event {

  implicit class ExtendEvent(ev: Event) {
  }

}
@JSGlobal
@js.native
class Event extends js.Any {
  val kind: String = js.native
  val bucket: String = js.native
  val contentType: String = js.native
  val id: String = js.native
  val name: String = js.native
}
