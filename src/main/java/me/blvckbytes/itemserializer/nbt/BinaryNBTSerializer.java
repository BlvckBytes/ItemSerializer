package me.blvckbytes.itemserializer.nbt;

import me.blvckbytes.bbreflection.ReflectionHelper;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class BinaryNBTSerializer extends ANBTSerializer<byte[]> {

  public BinaryNBTSerializer(ReflectionHelper rh) throws Exception {
    super(rh);
  }

  @Override
  public byte[] serialize(Object tag) throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    serializeSub(tag, os);
    return os.toByteArray();
  }

  @Override
  public Object deserialize(byte[] data) throws Exception {
    return deserializeSub(new ByteInput(data));
  }

  //=========================================================================//
  //                              Deserialization                            //
  //=========================================================================//

  /**
   * Subroutine for deserializing the next element indicated by a {@link TypeMarker}
   * @param input Byte input to deserialize from
   * @return Deserialized object
   */
  private Object deserializeSub(ByteInput input) throws Exception {
    byte marker = input.read();

    if (marker == TypeMarker.BYTE.getCorrespondingByte())
      return CT_NBT_TAG_BYTE.newInstance(input.read());

    if (marker == TypeMarker.SHORT.getCorrespondingByte())
      return CT_NBT_TAG_SHORT.newInstance(deserializeShort(input));

    if (marker == TypeMarker.INT.getCorrespondingByte())
      return CT_NBT_TAG_INT.newInstance(deserializeInt(input));

    if (marker == TypeMarker.LONG.getCorrespondingByte())
      return CT_NBT_TAG_LONG.newInstance(deserializeLong(input));

    if (marker == TypeMarker.FLOAT.getCorrespondingByte())
      return CT_NBT_TAG_FLOAT.newInstance(Float.intBitsToFloat(deserializeInt(input)));

    if (marker == TypeMarker.DOUBLE.getCorrespondingByte())
      return CT_NBT_TAG_DOUBLE.newInstance(Double.longBitsToDouble(deserializeLong(input)));

    if (marker == TypeMarker.STRING.getCorrespondingByte())
      return CT_NBT_TAG_STRING.newInstance(deserializeString(input));

    if (marker == TypeMarker.ARRAY.getCorrespondingByte())
      return deserializeList(input);

    if (marker == TypeMarker.OBJECT.getCorrespondingByte())
      return deserializeCompound(input);

    throw new IllegalStateException("Unknown marker=" + marker + " at pos=" + input.getPos());
  }

  /**
   * Deserializes a short from the input
   * @param input Input to serialize from
   * @return Short value
   */
  private short deserializeShort(ByteInput input) {
    int numBytes = Short.SIZE / 8;
    short result = 0;

    for (int i = 0; i < numBytes; i++)
      result |= input.read() << (i * 8);

    return result;
  }

  /**
   * Deserializes a long from the input
   * @param input Input to serialize from
   * @return Long value
   */
  private long deserializeLong(ByteInput input) {
    int numBytes = Long.SIZE / 8;
    long result = 0;

    for (int i = 0; i < numBytes; i++)
      result |= ((long) input.read()) << (i * 8);

    return result;
  }

  /**
   * Deserializes an int from the input
   * @param input Input to serialize from
   * @return Integer value
   */
  private int deserializeInt(ByteInput input) {
    int numBytes = Integer.SIZE / 8;
    int result = 0;

    for (int i = 0; i < numBytes; i++)
      result |= input.read() << (i * 8);

    return result;
  }

  /**
   * Deserializes a string from the input
   * @param input Input to serialize from
   * @return String value
   */
  private String deserializeString(ByteInput input) {
    int length = deserializeInt(input);
    return input.readString(length);
  }

  /**
   * Deserializes a list from the input by first reading it's length and then
   * adding n elements using {@link #deserializeSub}
   * @param input Input to serialize from
   * @return List containing read tags
   */
  @SuppressWarnings("unchecked")
  private Object deserializeList(ByteInput input) throws Exception {
    Object tag = CT_NBT_TAG_LIST.newInstance();
    List<Object> list = (List<Object>) F_NBT_TAG_LIST__VALUE.get(tag);
    int size = deserializeInt(input);

    F_NBT_TAG_LIST__TYPE.set(tag, input.read());

    for (int i = 0; i < size; i++)
      list.add(deserializeSub(input));

    return tag;
  }


  /**
   * Deserializes a map from the input by first reading it's length and then
   * adding n k-v pairs using {@link #deserializeSub}, where keys can only be strings
   * @param input Input to serialize from
   * @return Map containing read tag-pairs
   */
  @SuppressWarnings("unchecked")
  private Object deserializeCompound(ByteInput input) throws Exception {
    Object tag = CT_NBT_TAG_COMPOUND.newInstance();
    Map<Object, Object> map = (Map<Object, Object>) F_NBT_COMPOUND__VALUE.get(tag);
    int size = deserializeInt(input);

    for (int i = 0; i < size; i++) {
      String key = deserializeString(input);
      map.put(key, deserializeSub(input));
    }

    return tag;
  }

  //=========================================================================//
  //                               Serialization                             //
  //=========================================================================//

  /**
   * Subroutine for routing serialization for an NBT tag based on it's type
   * @param tag Tag to serialize
   * @param os Output stream to write bytes to
   */
  private void serializeSub(Object tag, ByteArrayOutputStream os) throws Exception {
    if (C_NBT_LIST.isInstance(tag)) {
      serializeList(tag, os);
      return;
    }

    if (C_NBT_TAG_COMPOUND.isInstance(tag)) {
      serializeCompound(tag, os);
      return;
    }

    if (C_NBT_NUMBER.isInstance(tag)) {
      serializeNumber(tag, os);
      return;
    }

    if (C_NBT_TAG_STRING.isInstance(tag)) {
      serializeString((String) F_NBT_TAG_STRING__VALUE.get(tag), true, os);
      return;
    }

    throw new IllegalStateException("Unsupported tag type encountered: " + tag.getClass());
  }

  /**
   * Serializes a tag compound by first writing the size integer and then writing
   * element after element as key-value pairs, where the key is a string and the
   * value is being written by making use of {@link #serializeSub}
   * @param tag Tag to serialize
   * @param os Output stream to write bytes to
   */
  private void serializeCompound(Object tag, ByteArrayOutputStream os) throws Exception {
    Map<?, ?> map = (Map<?, ?>) F_NBT_COMPOUND__VALUE.get(tag);

    os.write(TypeMarker.OBJECT.getCorrespondingByte());
    serializeInt(map.size(), os);

    for (Map.Entry<?, ?> entry : map.entrySet()) {
      // Map keys can only be strings, no need to add a separate marker byte
      serializeString((String) entry.getKey(), false, os);
      serializeSub(entry.getValue(), os);
    }
  }

  /**
   * Serializes a list compound by first writing the size integer and then writing
   * element after element, where the value is being written by making use of {@link #serializeSub}
   * @param tag Tag to serialize
   * @param os Output stream to write bytes to
   */
  private void serializeList(Object tag, ByteArrayOutputStream os) throws Exception {
    int size = (int) M_NBT_LIST__SIZE.invoke(tag);

    os.write(TypeMarker.ARRAY.getCorrespondingByte());
    serializeInt(size, os);
    os.write((byte) F_NBT_TAG_LIST__TYPE.get(tag));

    for (int i = 0; i < size; i++)
      serializeSub(M_NBT_LIST__GET.invoke(tag, i), os);
  }

  /**
   * Serializes a long to the output
   * @param value Value to serialize
   * @param os Output stream to write bytes to
   */
  private void serializeLong(long value, ByteArrayOutputStream os) {
    int numBytes = Long.SIZE / 8;
    for (int i = 0; i < numBytes; i++) {
      os.write((byte) (0xFF & value));
      value = value >> 8;
    }
  }

  /**
   * Serializes an integer to the output
   * @param value Value to serialize
   * @param os Output stream to write bytes to
   */
  private void serializeInt(int value, ByteArrayOutputStream os) {
    int numBytes = Integer.SIZE / 8;
    for (int i = 0; i < numBytes; i++) {
      os.write((byte) (0xFF & value));
      value = value >> 8;
    }
  }

  /**
   * Serializes a short to the output
   * @param value Value to serialize
   * @param os Output stream to write bytes to
   */
  private void serializeShort(short value, ByteArrayOutputStream os) {
    int numBytes = Short.SIZE / 8;
    for (int i = 0; i < numBytes; i++) {
      os.write((byte) (0xFF & value));
      value = (short) (value >> 8);
    }
  }

  /**
   * Serializes any number by first appending it's corresponding marker and then either calling
   * {@link #serializeInt}, {@link #serializeLong} or {@link #serializeShort} for whole numbers,
   * writing bytes as bytes and encoding floating-point numbers using integer/long bit representation.
   * @param tag Tag to serialize
   * @param os Output stream to write bytes to
   */
  private void serializeNumber(Object tag, ByteArrayOutputStream os) throws Exception {
    Number number = (Number) M_NBT_NUMBER__GET.invoke(tag);

    if (number instanceof Integer) {
      os.write(TypeMarker.INT.getCorrespondingByte());
      serializeInt(number.intValue(), os);
      return;
    }

    if (number instanceof Long) {
      os.write(TypeMarker.LONG.getCorrespondingByte());
      serializeLong(number.longValue(), os);
      return;
    }

    if (number instanceof Short) {
      os.write(TypeMarker.SHORT.getCorrespondingByte());
      serializeShort(number.shortValue(), os);
      return;
    }

    if (number instanceof Byte) {
      os.write(TypeMarker.BYTE.getCorrespondingByte());
      os.write(number.byteValue());
      return;
    }

    if (number instanceof Double) {
      long longRepr = Double.doubleToLongBits(number.doubleValue());
      os.write(TypeMarker.DOUBLE.getCorrespondingByte());
      serializeLong(longRepr, os);
      return;
    }

    if (number instanceof Float) {
      int intRepr = Float.floatToIntBits(number.floatValue());
      os.write(TypeMarker.FLOAT.getCorrespondingByte());
      serializeInt(intRepr, os);
      return;
    }

    throw new IllegalStateException("Unsupported number encountered: " + number.getClass());
  }

  /**
   * Serializes a string to the output by optionally appending a leading type marker, followed by
   * the string's byte length, followed by the individual bytes
   * @param string String to serialize
   * @param appendMarker Whether to append a leading type marker
   * @param os Output stream to write bytes to
   */
  private void serializeString(String string, boolean appendMarker, ByteArrayOutputStream os) {
    if (appendMarker)
      os.write(TypeMarker.STRING.getCorrespondingByte());

    byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
    serializeInt(bytes.length, os);

    for (byte b : bytes)
      os.write(b);
  }
}