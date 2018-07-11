package common

import java.time
import java.time.LocalDate

import scala.scalajs.js

object Date {
  def format(date: js.Date): String = {
    val month = date.getMonth() + 1
    val day = date.getDay()
    s"${date.getFullYear()}-${if (month < 10) s"0${month}" else month}-${if (day < 10) s"0${day}" else day}"
  }

  def format(showDate: LocalDate): String = {
    val month = if (showDate.getMonthValue < 10) s"0${showDate.getMonthValue}" else showDate.getMonthValue
    val day = if (showDate.getDayOfMonth < 10) s"0${showDate.getDayOfMonth}" else showDate.getDayOfMonth
    s"${showDate.getYear}-${month}-${day}"
  }

  def parse(str: String): java.time.LocalDate = {
    val jsDate = new js.Date(str)
    time.LocalDate.of(jsDate.getFullYear, jsDate.getMonth + 1, jsDate.getDate)
  }
}


