/*
 *   __   __     __  __     __         __
 *  /\ "-.\ \   /\ \/\ \   /\ \       /\ \
 *  \ \ \-.  \  \ \ \_\ \  \ \ \____  \ \ \____
 *   \ \_\\"\_\  \ \_____\  \ \_____\  \ \_____\
 *    \/_/ \/_/   \/_____/   \/_____/   \/_____/
 *   ______     ______       __     ______     ______     ______
 *  /\  __ \   /\  == \     /\ \   /\  ___\   /\  ___\   /\__  _\
 *  \ \ \/\ \  \ \  __<    _\_\ \  \ \  __\   \ \ \____  \/_/\ \/
 *   \ \_____\  \ \_____\ /\_____\  \ \_____\  \ \_____\    \ \_\
 *    \/_____/   \/_____/ \/_____/   \/_____/   \/_____/     \/_/
 *
 * https://joshbassett.info
 * https://twitter.com/nullobject
 * https://github.com/nullobject
 *
 * Copyright (c) 2021 Josh Bassett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package axon.mem

import chisel3._
import chiseltest._
import org.scalatest._

class MemArbiterTest extends FlatSpec with ChiselScalatestTester with Matchers {
  private def mkMemArbiter = new MemArbiter(2, 8, 8)

  it should "mux the request to the input port with the highest priority" in {
    test(mkMemArbiter) { dut =>
      // Read 0+1
      dut.io.in(0).rd.poke(true.B)
      dut.io.in(0).addr.poke(1.U)
      dut.io.in(1).rd.poke(true.B)
      dut.io.in(1).addr.poke(2.U)
      dut.io.out.rd.expect(true.B)
      dut.io.out.addr.expect(1.U)

      // Read 1
      dut.io.in(0).rd.poke(false.B)
      dut.io.out.rd.expect(true.B)
      dut.io.out.addr.expect(2.U)

      // Done
      dut.io.in(1).rd.poke(false.B)
      dut.io.out.rd.expect(false.B)
      dut.io.out.addr.expect(0.U)
    }
  }

  it should "assert the wait signal" in {
    test(mkMemArbiter) { dut =>
      dut.io.out.waitReq.poke(true.B)

      // Read 0+1
      dut.io.in(0).rd.poke(true.B)
      dut.io.in(1).rd.poke(true.B)
      dut.io.in(0).waitReq.expect(true.B)
      dut.io.in(1).waitReq.expect(true.B)

      // Wait 0
      dut.io.out.waitReq.poke(false.B)
      dut.io.in(0).waitReq.expect(false.B)
      dut.io.in(1).waitReq.expect(true.B)
      dut.io.out.waitReq.poke(true.B)
      dut.io.in(0).waitReq.expect(true.B)
      dut.io.in(1).waitReq.expect(true.B)

      // Read 1
      dut.io.in(0).rd.poke(false.B)
      dut.io.in(1).rd.poke(true.B)
      dut.io.in(0).waitReq.expect(false.B)
      dut.io.in(1).waitReq.expect(true.B)

      // Wait 1
      dut.io.out.waitReq.poke(false.B)
      dut.io.in(0).waitReq.expect(false.B)
      dut.io.in(1).waitReq.expect(false.B)
      dut.io.out.waitReq.poke(true.B)
      dut.io.in(0).waitReq.expect(false.B)
      dut.io.in(1).waitReq.expect(true.B)
    }
  }

  it should "assert the valid signal" in {
    test(mkMemArbiter) { dut =>
      // Read 0+1
      dut.io.in(0).rd.poke(true.B)
      dut.io.in(1).rd.poke(true.B)
      dut.clock.step()

      // Valid 0
      dut.io.out.valid.poke(true.B)
      dut.io.out.dout.poke(1.U)
      dut.io.in(0).valid.expect(true.B)
      dut.io.in(1).valid.expect(false.B)
      dut.io.in(0).dout.expect(1.U)
      dut.clock.step()

      // Burst done
      dut.io.out.burstDone.poke(true.B)
      dut.clock.step()
      dut.io.in(0).rd.poke(false.B)
      dut.clock.step()
      dut.io.out.burstDone.poke(false.B)

      // Valid 1
      dut.io.in(1).rd.poke(false.B)
      dut.io.out.valid.poke(true.B)
      dut.io.out.dout.poke(2.U)
      dut.io.in(0).valid.expect(false.B)
      dut.io.in(1).valid.expect(true.B)
      dut.io.in(1).dout.expect(2.U)
      dut.io.out.valid.poke(false.B)
    }
  }

  it should "assert the burst done signal" in {
    test(mkMemArbiter) { dut =>
      // Read 0+1
      dut.io.in(0).rd.poke(true.B)
      dut.io.in(1).rd.poke(true.B)
      dut.clock.step()

      // Burst done 0
      dut.io.out.burstDone.poke(true.B)
      dut.io.in(0).burstDone.expect(true.B)
      dut.io.in(1).burstDone.expect(false.B)
      dut.clock.step()
      dut.io.in(0).rd.poke(false.B)
      dut.clock.step()

      // Burst done 1
      dut.io.in(0).burstDone.expect(false.B)
      dut.io.in(1).burstDone.expect(true.B)
      dut.clock.step()
      dut.io.in(1).rd.poke(false.B)
      dut.io.in(0).burstDone.expect(false.B)
      dut.io.in(1).burstDone.expect(false.B)
    }
  }
}
