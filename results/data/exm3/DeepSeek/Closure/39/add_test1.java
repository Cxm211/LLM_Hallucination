// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testNestedRecordAnnotation() {
    JSType innerRecord = new RecordTypeBuilder(registry)
        .addProperty("b1", NUMBER_TYPE, null)
        .addProperty("b2", NUMBER_TYPE, null)
        .addProperty("b3", NUMBER_TYPE, null)
        .addProperty("b4", NUMBER_TYPE, null)
        .addProperty("b5", NUMBER_TYPE, null)
        .build();
    JSType outerRecord = new RecordTypeBuilder(registry)
        .addProperty("inner", innerRecord, null)
        .build();
    assertEquals("{inner: {b1: number, b2: number, b3: number, b4: number, b5: number}}",
        outerRecord.toAnnotationString());
  }
