package cinemaScheduleBatch


object ConvertCsv {
  def toCsv[C: ConvertCsv](c: C): Seq[String] = implicitly[ConvertCsv[C]].run(c)
}


trait ConvertCsv[E] {
  def run(e: E): Seq[String]
}
