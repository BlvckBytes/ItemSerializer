package me.blvckbytes.itemserializer;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.JsonPrimitive;
import me.blvckbytes.bbreflection.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class BinaryItemSerializer {

  protected final ClassHandle
    C_NBT_TAG_COMPOUND,
    C_NBT_TAG_STRING,
    C_NBT_NUMBER,
    C_NBT_LIST,
    C_NBT_TAG_INT,
    C_NBT_TAG_LONG,
    C_NBT_TAG_BYTE;

  protected final MethodHandle
    M_NBT_LIST__GET,
    M_NBT_LIST__SIZE,
    M_NBT_NUMBER__GET,
    M_NBT_BASE__TYPE,
    M_CIS__AS_CRAFT_COPY;

  protected final ConstructorHandle
    CT_NBT_TAG_STRING,
    CT_NBT_TAG_INT,
    CT_NBT_TAG_FLOAT,
    CT_NBT_TAG_LONG,
    CT_NBT_TAG_DOUBLE,
    CT_NBT_TAG_BYTE,
    CT_NBT_TAG_SHORT,
    CT_NBT_TAG_LIST,
    CT_NBT_TAG_COMPOUND,
    CT_NBT_TAG_INT_ARRAY,
    CT_NBT_TAG_BYTE_ARRAY,
    CT_NBT_TAG_LONG_ARRAY;

  protected final FieldHandle
    F_NBT_COMPOUND__VALUE,
    F_NBT_TAG_STRING__VALUE,
    F_NBT_TAG_LIST__VALUE,
    F_NBT_TAG_LIST__TYPE,
    F_NIS__TAG,
    F_CIS__HANDLE;

  protected final Field F_JSON_PRIMITIVE__VALUE;

  public BinaryItemSerializer(ReflectionHelper rh) throws Exception {
    ClassHandle C_NBT_TAG_DOUBLE = rh.getClass(RClass.NBT_TAG_DOUBLE);
    ClassHandle C_NBT_TAG_FLOAT = rh.getClass(RClass.NBT_TAG_FLOAT);
    ClassHandle C_NBT_TAG_SHORT = rh.getClass(RClass.NBT_TAG_SHORT);
    ClassHandle C_NBT_TAG_LIST = rh.getClass(RClass.NBT_TAG_LIST);
    ClassHandle C_NBT_BASE = rh.getClass(RClass.NBT_BASE);
    ClassHandle C_NMS_ITEM_STACK = rh.getClass(RClass.ITEM_STACK);
    ClassHandle C_CRAFT_ITEM_STACK = rh.getClass(RClass.CRAFT_ITEM_STACK);
    ClassHandle c_NBT_TAG_COMPOUND = rh.getClass(RClass.NBT_TAG_COMPOUND);

    C_NBT_TAG_COMPOUND = rh.getClass(RClass.NBT_TAG_COMPOUND);
    C_NBT_TAG_STRING = rh.getClass(RClass.NBT_TAG_STRING);
    C_NBT_LIST = rh.getClass(RClass.NBT_LIST);
    C_NBT_NUMBER = rh.getClass(RClass.NBT_NUMBER);
    C_NBT_TAG_INT = rh.getClass(RClass.NBT_TAG_INT);
    C_NBT_TAG_LONG = rh.getClass(RClass.NBT_TAG_LONG);
    C_NBT_TAG_BYTE = rh.getClass(RClass.NBT_TAG_BYTE);

    F_NBT_COMPOUND__VALUE = C_NBT_TAG_COMPOUND.locateField().withType(Map.class).withGeneric(String.class).required();
    F_NBT_TAG_STRING__VALUE = C_NBT_TAG_STRING.locateField().withType(String.class).required();
    F_NBT_TAG_LIST__VALUE = C_NBT_TAG_LIST.locateField().withType(List.class).required();
    F_NBT_TAG_LIST__TYPE = C_NBT_TAG_LIST.locateField().withType(byte.class).required();
    F_JSON_PRIMITIVE__VALUE = JsonPrimitive.class.getDeclaredField("value");
    F_JSON_PRIMITIVE__VALUE.setAccessible(true);
    F_NIS__TAG = C_NMS_ITEM_STACK.locateField().withType(c_NBT_TAG_COMPOUND).required();
    F_CIS__HANDLE = C_CRAFT_ITEM_STACK.locateField().withType(C_NMS_ITEM_STACK).required();

    M_NBT_NUMBER__GET = C_NBT_NUMBER.locateMethod().withAbstract(true).withReturnType(Number.class).required();
    M_NBT_LIST__GET = C_NBT_LIST.locateMethod().withAbstract(true).withParameters(int.class).withReturnType(Object.class, false, Assignability.TYPE_TO_TARGET).required();
    M_NBT_LIST__SIZE = C_NBT_LIST.locateMethod().withAbstract(true).withReturnType(int.class).required();
    M_NBT_BASE__TYPE = C_NBT_BASE.locateMethod().withAbstract(true).withReturnType(byte.class).required();
    M_CIS__AS_CRAFT_COPY = C_CRAFT_ITEM_STACK.locateMethod().withName("asCraftCopy").withStatic(true).required();

    CT_NBT_TAG_STRING = C_NBT_TAG_STRING.locateConstructor().withParameters(String.class).required();
    CT_NBT_TAG_INT = C_NBT_TAG_INT.locateConstructor().withParameter(Integer.class, true, Assignability.NONE).required();
    CT_NBT_TAG_FLOAT = C_NBT_TAG_FLOAT.locateConstructor().withParameter(Float.class, true, Assignability.NONE).required();
    CT_NBT_TAG_LONG = C_NBT_TAG_LONG.locateConstructor().withParameter(Long.class, true, Assignability.NONE).required();
    CT_NBT_TAG_DOUBLE = C_NBT_TAG_DOUBLE.locateConstructor().withParameter(Double.class, true, Assignability.NONE).required();
    CT_NBT_TAG_BYTE = C_NBT_TAG_BYTE.locateConstructor().withParameter(Byte.class, true, Assignability.NONE).required();
    CT_NBT_TAG_SHORT = C_NBT_TAG_SHORT.locateConstructor().withParameter(Short.class, true, Assignability.NONE).required();
    CT_NBT_TAG_LIST = C_NBT_TAG_LIST.locateConstructor().required();
    CT_NBT_TAG_COMPOUND = C_NBT_TAG_COMPOUND.locateConstructor().required();
    CT_NBT_TAG_INT_ARRAY = rh.getClass(RClass.NBT_TAG_INT_ARRAY).locateConstructor().withParameters(int[].class).required();
    CT_NBT_TAG_BYTE_ARRAY = rh.getClass(RClass.NBT_TAG_BYTE_ARRAY).locateConstructor().withParameters(byte[].class).required();
    CT_NBT_TAG_LONG_ARRAY = rh.getClass(RClass.NBT_TAG_LONG_ARRAY).locateConstructor().withParameters(long[].class).required();
  }

  public void serialize(ItemStack item, ByteArrayOutputStream os) throws Exception {
    Object handle = F_CIS__HANDLE.get(item);
    Object tag = F_NIS__TAG.get(handle);

    serializeString(XMaterial.matchXMaterial(item).name(), false, os);
    serializeInt(item.getAmount(), os);
    serializeSub(tag, os);
  }

  public ItemStack deserialize(ByteBufferInput input) throws Exception {
    XMaterial material = XMaterial.valueOf(deserializeString(input));
    ItemStack item = new ItemStack(Material.BARRIER, deserializeInt(input));
    material.setType(item);

    Object tag = deserializeSub(input);

    Object bukkitItem = M_CIS__AS_CRAFT_COPY.invoke(null, item);
    Object handle = F_CIS__HANDLE.get(bukkitItem);
    F_NIS__TAG.set(handle, tag);

    return (ItemStack) bukkitItem;
  }

  //=========================================================================//
  //                              Deserialization                            //
  //=========================================================================//

  /**
   * Subroutine for deserializing the next element indicated by a {@link TypeMarker}
   * @param input Byte input to deserialize from
   * @return Deserialized object
   */
  private Object deserializeSub(ByteBufferInput input) throws Exception {
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
  private short deserializeShort(ByteBufferInput input) {
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
  private long deserializeLong(ByteBufferInput input) {
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
  private int deserializeInt(ByteBufferInput input) {
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
  private String deserializeString(ByteBufferInput input) {
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
  private Object deserializeList(ByteBufferInput input) throws Exception {
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
  private Object deserializeCompound(ByteBufferInput input) throws Exception {
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