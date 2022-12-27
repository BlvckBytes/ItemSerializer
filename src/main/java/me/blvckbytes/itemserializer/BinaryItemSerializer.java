package me.blvckbytes.itemserializer;

import com.cryptomorin.xseries.XMaterial;
import me.blvckbytes.bbreflection.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public class BinaryItemSerializer {

  private final MethodHandle M_CIS__AS_CRAFT_COPY, M_NBT_TAG_COMPOUND__WRITE, M_NBT_TAG_COMPOUND__LOAD;
  private final ConstructorHandle CT_NBT_TAG_COMPOUND;
  private final FieldHandle F_NIS__TAG, F_CIS__HANDLE;
  private final Object INST_NBT_READ_LIMITER_NONE;

  public BinaryItemSerializer(ReflectionHelper rh) throws Exception {
    ClassHandle C_CRAFT_ITEM_STACK = rh.getClass(RClass.CRAFT_ITEM_STACK);
    ClassHandle C_NBT_READ_LIMITER = rh.getClass(RClass.NBT_READ_LIMITER);
    ClassHandle C_NMS_ITEM_STACK = rh.getClass(RClass.ITEM_STACK);
    ClassHandle c_NBT_TAG_COMPOUND = rh.getClass(RClass.NBT_TAG_COMPOUND);

    // There's a noop implementation of the read-limiter available as a static self-type variable within it's class
    INST_NBT_READ_LIMITER_NONE = C_NBT_READ_LIMITER.locateField()
      .withType(C_NBT_READ_LIMITER, false, Assignability.TARGET_TO_TYPE)
      .withStatic(true)
      .required()
      .get(null);

    M_CIS__AS_CRAFT_COPY = C_CRAFT_ITEM_STACK.locateMethod().withName("asCraftCopy").withStatic(true).required();
    M_NBT_TAG_COMPOUND__WRITE = c_NBT_TAG_COMPOUND.locateMethod().withParameters(DataOutput.class).required();
    M_NBT_TAG_COMPOUND__LOAD = c_NBT_TAG_COMPOUND.locateMethod().withParameters(DataInput.class, int.class, C_NBT_READ_LIMITER.get()).required();
    F_NIS__TAG = C_NMS_ITEM_STACK.locateField().withType(c_NBT_TAG_COMPOUND).required();
    F_CIS__HANDLE = C_CRAFT_ITEM_STACK.locateField().withType(C_NMS_ITEM_STACK).required();
    CT_NBT_TAG_COMPOUND = c_NBT_TAG_COMPOUND.locateConstructor().required();
  }

  public void serialize(ItemStack item, DataOutputStream os) throws Exception {
    // Get the root NBTTagCompound node from the CraftBukkitItem's underlying NMS handle
    Object handle = F_CIS__HANDLE.get(item);
    Object tag = F_NIS__TAG.get(handle);

    // Write the item's corresponding XMaterial name as a string and it's amount as an integer
    DataOutputStream dos = new DataOutputStream(os);
    dos.writeUTF(XMaterial.matchXMaterial(item).name());
    dos.writeInt(item.getAmount());

    // Serialize the NBT tag
    M_NBT_TAG_COMPOUND__WRITE.invoke(tag, dos);
    dos.close();
  }

  public ItemStack deserialize(DataInputStream is) throws Exception {
    // Read the item's corresponding XMaterial name as a string
    XMaterial material = XMaterial.valueOf(is.readUTF());

    // Create a "fallback item" of the stored amount
    ItemStack item = new ItemStack(Material.BARRIER, is.readInt());

    // Apply XMaterial
    material.setType(item);

    // Create a new empty NBTTagCompound and deserialize it's contents
    Object tag = CT_NBT_TAG_COMPOUND.newInstance();
    M_NBT_TAG_COMPOUND__LOAD.invoke(tag, is, 0, INST_NBT_READ_LIMITER_NONE);

    // Create a new CraftBukkitItem to have it's underlying NMS handle available
    Object bukkitItem = M_CIS__AS_CRAFT_COPY.invoke(null, item);
    Object handle = F_CIS__HANDLE.get(bukkitItem);

    // Set the deserialized NBTTagCompound reference
    F_NIS__TAG.set(handle, tag);
    return (ItemStack) bukkitItem;
  }
}