/*
 *    __   __     __  __     __         __
 *   /\ "-.\ \   /\ \/\ \   /\ \       /\ \
 *   \ \ \-.  \  \ \ \_\ \  \ \ \____  \ \ \____
 *    \ \_\\"\_\  \ \_____\  \ \_____\  \ \_____\
 *     \/_/ \/_/   \/_____/   \/_____/   \/_____/
 *    ______     ______       __     ______     ______     ______
 *   /\  __ \   /\  == \     /\ \   /\  ___\   /\  ___\   /\__  _\
 *   \ \ \/\ \  \ \  __<    _\_\ \  \ \  __\   \ \ \____  \/_/\ \/
 *    \ \_____\  \ \_____\ /\_____\  \ \_____\  \ \_____\    \ \_\
 *     \/_____/   \/_____/ \/_____/   \/_____/   \/_____/     \/_/
 *
 *  https://joshbassett.info
 *  https://twitter.com/nullobject
 *  https://github.com/nullobject
 *
 *  Copyright (c) 2020 Josh Bassett
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package axon.mem

import chisel3._

/** Represents a memory request. */
class MemRequest[S <: Data, T <: Data](s: S, t: T) extends Bundle {
  /** Read enable */
  val rd = Output(Bool())
  /** Write enable */
  val wr = Output(Bool())
  /** Address bus */
  val addr = Output(s)
  /** Data bus */
  val data = Output(t)

  /** Returns true if the read or write enable flag is asserted */
  def valid: Bool = rd || wr

  override def cloneType: this.type = new MemRequest(s, t).asInstanceOf[this.type]
}

object MemRequest {
  /**
   * Constructs a memory request.
   *
   * @param rd   Read enable.
   * @param wr   Write enable.
   * @param addr The address value.
   * @param data The data value.
   */
  def apply[S <: Data, T <: Data](rd: Bool, wr: Bool, addr: S, data: T): MemRequest[S, T] = {
    val req = Wire(new MemRequest(chiselTypeOf(addr), chiselTypeOf(data)))
    req.rd := rd
    req.wr := wr
    req.addr := addr
    req.data := data
    req
  }
}