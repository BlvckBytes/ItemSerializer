package me.blvckbytes.itemserializer.nbt;

public interface INBTSerializer<T> {

  T serialize(Object tag) throws Exception;

  Object deserialize(T data) throws Exception;

}
