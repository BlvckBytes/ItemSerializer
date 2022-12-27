package me.blvckbytes.itemserializer;

import java.nio.charset.StandardCharsets;

public class ByteBufferInput {

  private final byte[] buf;
  private int pos;

  /**
   * Create a new byte input based on an underlying byte buffer
   * @param buf Byte buffer to incrementally read
   */
  public ByteBufferInput(byte[] buf) {
    this.buf = buf;
  }

  /**
   * Read the next byte or throw an {@link IllegalStateException} when reaching the end
   */
  public byte read() {
    if (pos == buf.length)
      throw new IllegalStateException("Unexpected end of buffer reached");
    return buf[pos++];
  }

  /**
   * Get the current buffer position
   */
  public int getPos() {
    return Math.max(0, pos - 1);
  }

  /**
   * Read a string from a sub-array of the internal buffer or throw an
   * {@link IllegalStateException} if the buffer isn't long enough
   * @param length Length of the string to read
   * @return Read string
   */
  public String readString(int length) {
    if (pos + length > buf.length)
      throw new IllegalStateException("Could not read string of length=" + length + ", buffer too short");

    String str = new String(buf, pos, length, StandardCharsets.UTF_8);
    pos += length;

    return str;
  }
}
