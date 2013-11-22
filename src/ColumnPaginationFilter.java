/*
 * Copyright (C) 2013  The Async HBase Authors.  All rights reserved.
 * This file is part of Async HBase.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the StumbleUpon nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.hbase.async;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Filters based on a range of column qualifiers.
 * @since 1.5
 */
public final class ColumnPaginationFilter extends ScanFilter {

  private static final byte[] NAME = Bytes.ISO88591("org.apache.hadoop.hbase"
      + ".filter.ColumnPaginationFilter");

  private int limit = 0;
  private int offset = -1;
  private int count = 0;

  /**
   * Constructor for numerical offset.
   * @param limit The maximum number of columns to return.
   * @param offset The integer offset where to start pagination.
   */
  public ColumnPaginationFilter(final int limit, final int offset) {
    this.limit = limit;
    this.offset = offset;
  }


  @Override
  int predictSerializedSize() {
    return 1 + NAME.length
      + 1 + varintSize(limit)
      + 1 + varintSize(offset);
  }

  int varintSize(int value) {
    if ((value & (0xffffffff <<  7)) == 0) return 1;
    if ((value & (0xffffffff << 14)) == 0) return 2;
    if ((value & (0xffffffff << 21)) == 0) return 3;
    if ((value & (0xffffffff << 28)) == 0) return 4;
    return 5;
  }

  void writeVarint(ChannelBuffer buf, int value) {
    while (true) {
      if ((value & ~0x7F) == 0) {
        buf.writeByte(value);
        return;
      } else {
        buf.writeByte((value & 0x7F) | 0x80);
        value >>>= 7;
      }
    }
  }

  @Override
  void serialize(ChannelBuffer buf) {
    buf.writeByte((byte) NAME.length);
    buf.writeBytes(NAME);

    // Limit
    writeVarint(buf, 8); // Tag
    writeVarint(buf, this.limit);

    // Integer Offset
    writeVarint(buf, 16); // Tag
    writeVarint(buf, this.offset);
  }

  public void getSerialize(ChannelBuffer buf) {
    serialize(buf);
  }

  public String toString() {
    return "ColumnPaginationFilter(limit=" + limit
      + ", offset=" + offset + ")";
  }

}
