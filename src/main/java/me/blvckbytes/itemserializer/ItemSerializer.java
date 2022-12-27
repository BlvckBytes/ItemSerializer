package me.blvckbytes.itemserializer;

import me.blvckbytes.bbreflection.*;
import me.blvckbytes.itemserializer.nbt.INBTSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSerializer {

  private final MethodHandle M_CIS__AS_CRAFT_COPY;
  private final FieldHandle F_NIS__TAG, F_CIS__HANDLE;

  public ItemSerializer(ReflectionHelper rh) throws Exception {
    ClassHandle C_NMS_ITEM_STACK = rh.getClass(RClass.ITEM_STACK);
    ClassHandle C_CRAFT_ITEM_STACK = rh.getClass(RClass.CRAFT_ITEM_STACK);
    ClassHandle c_NBT_TAG_COMPOUND = rh.getClass(RClass.NBT_TAG_COMPOUND);

    F_NIS__TAG = C_NMS_ITEM_STACK.locateField().withType(c_NBT_TAG_COMPOUND).required();
    F_CIS__HANDLE = C_CRAFT_ITEM_STACK.locateField().withType(C_NMS_ITEM_STACK).required();
    M_CIS__AS_CRAFT_COPY = C_CRAFT_ITEM_STACK.locateMethod().withName("asCraftCopy").withStatic(true).required();
  }

  public <T> T serialize(ItemStack item, INBTSerializer<T> serializer) throws Exception {
    Object handle = F_CIS__HANDLE.get(item);
    Object tag = F_NIS__TAG.get(handle);
    return serializer.serialize(tag);
  }

  public <T> ItemStack deserialize(T nbtData, Material material, int amount, INBTSerializer<T> serializer) throws Exception {
    Object tag = serializer.deserialize(nbtData);
    Object bukkitItem = M_CIS__AS_CRAFT_COPY.invoke(null, new ItemStack(material, amount));
    Object handle = F_CIS__HANDLE.get(bukkitItem);
    F_NIS__TAG.set(handle, tag);
    return (ItemStack) bukkitItem;
  }
}
