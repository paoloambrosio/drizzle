package net.paoloambrosio.drizzle.utils

import scala.language.implicitConversions

/**
  * Context-dependent Stream
  */
trait CDStream[C, T] {
  def apply(c: C): CDStreamEntry[C, T]
  def append(rest: => CDStream[C, T]): CDStream[C, T]
  def map[V](f: T => V): CDStream[C, V]
//  def flatMap[V](f: (C => T) => CDStream[C, V]): CDStream[C, V]
}

trait CDStreamEntry[C, T] {
  def isEmpty: Boolean
  def head: T
  def tail: CDStream[C, T]
}

object CDStream {

  implicit def cdStreamWrapper[C, T](stream: => CDStream[C, T]): CDStreamOperations[C, T] =
    new CDStreamOperations[C, T](stream)

  class CDStreamOperations[C, T](cds: => CDStream[C, T]) {
    def @::(hd: T): CDStream[C, T] = new StaticCDStream(hd, cds)
    def @:::(prefix: CDStream[C, T]): CDStream[C, T] = prefix append cds
  }

  object @:: {
    def unapply[C, T](cds: CDStream[C, T])(implicit c: C): Option[(T, CDStream[C, T])] = {
      val entry = cds.apply(c)
      if (entry.isEmpty) None
      else Some((entry.head, entry.tail))
    }
  }

  // TODO *variance

  def empty[C, T]: CDStream[C, T] = new CDStream[C, T] {
    override def apply(c: C) = new CDStreamEntry[C, T] {
      override val isEmpty = true
      override def head = throw new NoSuchElementException("head of empty stream")
      override def tail = throw new UnsupportedOperationException("tail of empty stream")
    }
    override def append(rest: => CDStream[C, T]): CDStream[C, T] = rest
    override def map[V](f: T => V): CDStream[C, V] = empty[C ,V]
  }

  def static[C, T](xs: T*): CDStream[C, T] = static(xs.toIterable)

  def static[C, T](it: Iterable[T]): CDStream[C, T] = it.toStream match {
    case hd #:: tail => new StaticCDStream(hd, static(tail))
    case _ => empty[C, T]
  }

  private class StaticCDStream[C, T](hd: T, tl: CDStream[C, T]) extends CDStream[C, T] {
    override def apply(c: C) = new CDStreamEntry[C, T] {
      override val isEmpty = false
      override def head = hd
      override def tail = tl
    }

    override def append(rest: => CDStream[C, T]): CDStream[C, T] =
      new StaticCDStream(hd, tl append rest)

    override def map[V](f: T => V): CDStream[C, V] =
      new StaticCDStream[C, V](f(hd), tl.map(f)) // TODO lazy
  }

  /**
    * Dynamically generate CDStream based on context
    */

  def loop[C, T](check: C => Boolean, body: CDStream[C, T]): CDStream[C, T] =
    dynamic(c => if (check(c)) Some(body) else None, true)

  def conditional[C, T](check: C => Boolean, body: CDStream[C, T]): CDStream[C, T] =
    dynamic(c => if (check(c)) Some(body) else None, false)

  private def dynamic[C, T](gen: C => Option[CDStream[C, T]], loop: Boolean): CDStream[C, T] = new DynamicCDStream(gen, loop, empty)

  class DynamicCDStream[C, T](gen: C => Option[CDStream[C, T]], loop: Boolean, next: CDStream[C, T]) extends CDStream[C, T] {
    override def apply(c: C): CDStreamEntry[C, T] = {
      val tl = if (loop) this else this.next
      (gen(c) map (_ @::: tl) getOrElse next)(c)
    }

    override def append(rest: => CDStream[C, T]): CDStream[C, T] =
      new DynamicCDStream(gen, loop, next append rest)

    override def map[V](f: T => V): CDStream[C, V] =
      new DynamicCDStream(gen.andThen(_ map (_ map f)), loop, next map f)
  }
}
