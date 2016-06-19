package loader

trait Loader {
  type F
  type U
  def open(t: U): F
  def close(source: F): Unit
  def readAll(source: F): Iterator[U]
}

object FileLoader extends Loader {
  type F = Source
  type U = String

  def open(t: String) = {
    Source.fromFile(t)
  }
  def readAll(source: F): Iterator[String] = {
    source.getLines()
  }
  def close(source: F): Unit = source.close()
}