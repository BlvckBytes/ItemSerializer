package me.blvckbytes.itemserializer.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.blvckbytes.bbreflection.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class JsonNBTSerializer extends ANBTSerializer<JsonElement> {

  public JsonNBTSerializer(ReflectionHelper rh) throws Exception {
    super(rh);
  }

  @Override
  public JsonElement serialize(@Nullable Object tag) throws Exception {
    if (tag == null)
      return new JsonObject();

    if (C_NBT_LIST.isInstance(tag))
      return serializeList(tag);

    if (C_NBT_TAG_COMPOUND.isInstance(tag))
      return serializeCompound(tag);

    if (C_NBT_NUMBER.isInstance(tag))
      return new JsonPrimitive((Number) M_NBT_NUMBER__GET.invoke(tag));

    if (C_NBT_TAG_STRING.isInstance(tag))
      return new JsonPrimitive((String) F_NBT_TAG_STRING__VALUE.get(tag));

    throw new IllegalStateException("Unsupported tag type encountered: " + tag.getClass());
  }

  @Override
  public Object deserialize(JsonElement data) throws Exception {
    if (data instanceof JsonPrimitive)
      return deserializePrimitive((JsonPrimitive) data);

    if (data instanceof JsonArray)
      return deserializeList((JsonArray) data);

    if (data instanceof JsonObject)
      return deserializeCompound((JsonObject) data);

    throw new IllegalStateException("Unsupported json element encountered: " + data);
  }

  //=========================================================================//
  //                             Deserialization                             //
  //=========================================================================//

  /**
   * Copies a number into a corresponding NBT tag instance
   * @param value Number to wrap
   * @return NBT tag instance containing this value
   */
  private Object deserializeNumber(Number value) throws Exception {
    if (value instanceof Integer)
      return CT_NBT_TAG_INT.newInstance(value.intValue());

    else if (value instanceof Long)
      return CT_NBT_TAG_LONG.newInstance(value.longValue());

    else if (value instanceof Float)
      return CT_NBT_TAG_FLOAT.newInstance(value.floatValue());

    else if (value instanceof Double)
      return CT_NBT_TAG_DOUBLE.newInstance(value.doubleValue());

    else if (value instanceof Byte)
      return CT_NBT_TAG_BYTE.newInstance(value.byteValue());

    else if (value instanceof Short)
      return CT_NBT_TAG_SHORT.newInstance(value.shortValue());

    throw new IllegalStateException("Unsupported number encountered: " + value);
  }

  /**
   * Copies a json primitive (string, number) into a corresponding NBT tag instance
   * @param primitive Primitive to wrap
   * @return NBT tag instance containing this value
   */
  private Object deserializePrimitive(JsonPrimitive primitive) throws Exception {
    if (primitive.isString())
      return CT_NBT_TAG_STRING.newInstance(primitive.getAsString());

    if (primitive.isNumber())
      return deserializeNumber(primitive.getAsNumber());

    throw new IllegalStateException("Unsupported json primitive encountered: " + primitive);
  }

  /**
   * Copies a json array of same-type primitives (int, long, byte) into
   * a corresponding NBT array instance
   * @param array Array to wrap
   * @return NBT array instance containing this array
   */
  private Object deserializeListOfSameType(JsonArray array) throws Exception {
    if (array.size() == 0)
      throw new IllegalStateException("Cannot detect primitive type on an empty array");

    Object typedArray = null;
    Class<?> firstClass = null;

    for (int i = 0; i < array.size(); i++) {
      Object value = deserializePrimitive((JsonPrimitive) array.get(i));

      if (typedArray == null) {
        firstClass = value.getClass();
        typedArray = Array.newInstance(firstClass, array.size());
      }

      Array.set(typedArray, i, value);
    }

    if (C_NBT_TAG_INT.get() == firstClass)
      return CT_NBT_TAG_INT_ARRAY.newInstance(typedArray);

    if (C_NBT_TAG_BYTE.get() == firstClass)
      return CT_NBT_TAG_BYTE_ARRAY.newInstance(typedArray);

    if (C_NBT_TAG_LONG.get() == firstClass)
      return CT_NBT_TAG_LONG_ARRAY.newInstance(typedArray);

    throw new IllegalStateException("Unsupported primitive tag type: " + firstClass);
  }

  /**
   * Determines the class of the primitives within this array if they're all the same
   * and the array is not empty, yields null otherwise
   * @param array Array to check
   */
  private @Nullable Class<?> getOnlyPrimitiveClass(JsonArray array) throws Exception {
    if (array.size() == 0)
      return null;

    Class<?> lastType = null;
    boolean allSamePrimitives = true;
    for (JsonElement item : array) {
      if (!(item instanceof JsonPrimitive)) {
        allSamePrimitives = false;
        break;
      }

      Class<?> currType = F_JSON_PRIMITIVE__VALUE.get(item).getClass();

      if (lastType == null) {
        lastType = currType;
        continue;
      }

      if (lastType == currType)
        continue;

      allSamePrimitives = false;
      break;
    }

    if (!allSamePrimitives)
      return null;

    return lastType;
  }

  /**
   * Copies a json array into either an NBT array or an nbt tag list, depending
   * on whether all values have the same numeric type or not
   * @param array Array to wrap
   * @return NBT array or tag list instance containing this array
   */
  @SuppressWarnings("unchecked")
  private Object deserializeList(JsonArray array) throws Exception {
    if (array.size() == 0)
      return CT_NBT_TAG_LIST.newInstance();

    if (getOnlyPrimitiveClass(array) != null)
      return deserializeListOfSameType(array);

    Object result = CT_NBT_TAG_LIST.newInstance();
    List<Object> list = (List<Object>) F_NBT_TAG_LIST__VALUE.get(result);

    for (JsonElement item : array)
      list.add(deserialize(item));

    Object type = M_NBT_BASE__TYPE.invoke(list.get(0));
    F_NBT_TAG_LIST__TYPE.set(result, type);

    return result;
  }

  /**
   * Copies a json object into a nbt tag compound
   * @param object Object to wrap
   * @return NBT tag compound containing this value
   */
  @SuppressWarnings("unchecked")
  private Object deserializeCompound(JsonObject object) throws Exception {
    Object result = CT_NBT_TAG_COMPOUND.newInstance();
    Map<String, Object> map = (Map<String, Object>) F_NBT_COMPOUND__VALUE.get(result);

    for (Map.Entry<String, JsonElement> entry : object.entrySet())
      map.put(entry.getKey(), deserialize(entry.getValue()));

    return result;
  }

  //=========================================================================//
  //                               Serialization                             //
  //=========================================================================//

  /**
   * Copies a nbt list tag into a json array
   * @param tag List tag to copy
   * @return Json array containing all items
   */
  private JsonArray serializeList(Object tag) throws Exception {
    JsonArray result = new JsonArray();
    int size = (int) M_NBT_LIST__SIZE.invoke(tag);

    for (int i = 0; i < size; i++)
      result.add(serialize(M_NBT_LIST__GET.invoke(tag, i)));

    return result;
  }

  /**
   * Copies a nbt tag compound into a json object
   * @param tag Tag compound to copy
   * @return Json object containing all entries
   */
  private JsonObject serializeCompound(Object tag) throws Exception {
    Map<?, ?> map = (Map<?, ?>) F_NBT_COMPOUND__VALUE.get(tag);
    JsonObject result = new JsonObject();

    for (Map.Entry<?, ?> entry : map.entrySet()) {
      Object key = entry.getKey();

      if (!(key instanceof String))
        throw new IllegalStateException("Cannot copy non-string key");

      result.add((String) key, serialize(entry.getValue()));
    }

    return result;
  }
}
