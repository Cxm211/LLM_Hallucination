// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testExactlyMaxPropertiesNoEllipsis() {
  JSType record = new RecordTypeBuilder(registry)
      .addProperty("p1", NUMBER_TYPE, null)
      .addProperty("p2", NUMBER_TYPE, null)
      .addProperty("p3", NUMBER_TYPE, null)
      .addProperty("p4", NUMBER_TYPE, null)
      .build();
  assertEquals("{p1: number, p2: number, p3: number, p4: number}",
      record.toString());
  assertEquals("{p1: number, p2: number, p3: number, p4: number}",
      record.toAnnotationString());
}