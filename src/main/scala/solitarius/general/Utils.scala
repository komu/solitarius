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

import java.util.{ Arrays, Collections }

object Utils {

  /** Returns a shuffled array containing elements of given collection */
  def shuffled[T](items: Iterable[T]) (implicit manifest: scala.reflect.ClassManifest[T]): Array[T] = {
    val array = items.toArray
    Collections.shuffle(Arrays.asList(array))
    array
  }

  def replicate[T](count: Int, items: List[T]): List[T] =
    List.fill(count)(items).flatten
}
