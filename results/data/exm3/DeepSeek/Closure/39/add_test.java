// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testExactlyMaxPropertiesAnnotation() {
    JSType record = new RecordTypeBuilder(registry)
        .addProperty("a1", NUMBER_TYPE, null)
        .addProperty("a2", NUMBER_TYPE, null)
        .addProperty("a3", NUMBER_TYPE, null)
        .addProperty("a4", NUMBER_TYPE, null)
        .build();
    assertEquals("{a1: number, a2: number, a3: number, a4: number}",
        record.toAnnotationString());
  }
