package me.blvckbytes.itemserializer.nbt;

import com.google.gson.JsonPrimitive;
import me.blvckbytes.bbreflection.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public abstract class ANBTSerializer<T> implements INBTSerializer<T> {

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
    M_NBT_BASE__TYPE;

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
    F_NBT_TAG_LIST__TYPE;

  protected final Field F_JSON_PRIMITIVE__VALUE;

  public ANBTSerializer(ReflectionHelper rh) throws Exception {

    ClassHandle C_NBT_TAG_DOUBLE = rh.getClass(RClass.NBT_TAG_DOUBLE);
    ClassHandle C_NBT_TAG_FLOAT = rh.getClass(RClass.NBT_TAG_FLOAT);
    ClassHandle C_NBT_TAG_SHORT = rh.getClass(RClass.NBT_TAG_SHORT);
    ClassHandle C_NBT_TAG_LIST = rh.getClass(RClass.NBT_TAG_LIST);
    ClassHandle C_NBT_BASE = rh.getClass(RClass.NBT_BASE);

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

    M_NBT_NUMBER__GET = C_NBT_NUMBER.locateMethod().withAbstract(true).withReturnType(Number.class).required();
    M_NBT_LIST__GET = C_NBT_LIST.locateMethod().withAbstract(true).withParameters(int.class).withReturnType(Object.class, false, Assignability.TYPE_TO_TARGET).required();
    M_NBT_LIST__SIZE = C_NBT_LIST.locateMethod().withAbstract(true).withReturnType(int.class).required();
    M_NBT_BASE__TYPE = C_NBT_BASE.locateMethod().withAbstract(true).withReturnType(byte.class).required();

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
}
