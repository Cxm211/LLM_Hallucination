// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testLongToAnnotationStringWithLoop() {
  ProxyObjectType loop = new ProxyObjectType(registry, NUMBER_TYPE);
  JSType record = new RecordTypeBuilder(registry)
      .addProperty("a1", loop, null)
      .addProperty("a2", NUMBER_TYPE, null)
      .addProperty("a3", NUMBER_TYPE, null)
      .addProperty("a4", NUMBER_TYPE, null)
      .addProperty("a5", NUMBER_TYPE, null)
      .addProperty("a6", NUMBER_TYPE, null)
      .build();
  loop.setReferencedType(record);
  String result = record.toAnnotationString();
  assertEquals("{a1: ?, a2: number, a3: number, a4: number, a5: number, a6: number}",
      result);
}