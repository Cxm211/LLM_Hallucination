// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testSubtypeWithUnknownReturnTypes() throws Exception {
    JSType recordA = new RecordTypeBuilder(registry)
        .addProperty("a",
            new FunctionBuilder(registry)
            .withReturnType(STRING_TYPE)
            .build(),
            null)
        .build();
    JSType recordB = new RecordTypeBuilder(registry)
        .addProperty("a",
            new FunctionBuilder(registry)
            .withReturnType(UNKNOWN_TYPE)
            .build(),
            null)
        .build();
    assertTrue(recordA.isSubtype(recordB));
    assertTrue(recordB.isSubtype(recordA));
  }