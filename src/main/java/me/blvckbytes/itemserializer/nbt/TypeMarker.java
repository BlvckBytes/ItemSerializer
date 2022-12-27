package me.blvckbytes.itemserializer.nbt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeMarker {
  SHORT  ((byte) 0x01),
  BYTE   ((byte) 0x02),
  INT    ((byte) 0x03),
  LONG   ((byte) 0x04),
  FLOAT  ((byte) 0x05),
  DOUBLE ((byte) 0x06),
  STRING ((byte) 0x07),
  ARRAY  ((byte) 0x08),
  OBJECT ((byte) 0x09)
  ;

  private final byte correspondingByte;
}
