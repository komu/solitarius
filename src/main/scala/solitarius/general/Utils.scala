/*
 *  Copyright 2008-2011 Juha Komulainen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package solitarius.general

import java.util.Random

object Utils {
  
  private val random = new Random

  /** Poor man's profiling */
  def time[T](name: String) (thunk: => T) {
    val start = currentTime
    try {
      thunk
    } finally {
      val total = currentTime - start
      println(name + ": " + total + "ms")
    }
  }
  
  def sum(xs: Seq[Int]) = xs.foldLeft(0)(_+_)
  
  def currentTime = System.currentTimeMillis
  
  /** Returns a shuffled array containing elements of given collection */
  def shuffled[T](items: Collection[T]) (implicit manifest: scala.reflect.ClassManifest[T]): Array[T] = {
    val array = items.toArray
    shuffle(array)
    array
  }
  
  def replicate[T](count: Int, items: List[T]): List[T] =
    if (count == 0) Nil else items ::: replicate(count-1, items)

  /** Shuffles given array in-place */
  def shuffle[T](array: Array[T]) =
    for (i <- Iterator.range(array.size, 1, -1))
      swap(array, i - 1, random.nextInt(i))

  private def swap[T](array: Array[T], i: Int, j: Int) = {
    val tmp = array(i)
    array(i) = array(j)
    array(j) = tmp
  }
}
