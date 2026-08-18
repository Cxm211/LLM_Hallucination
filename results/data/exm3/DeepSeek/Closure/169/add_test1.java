// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testSubtypeWithDeclaredPropertyNotInvariant() throws Exception {
    RecordType recordA = new RecordTypeBuilder(registry)
        .addProperty("a", NUMBER_TYPE, null)
        .build();
    RecordType recordB = new RecordTypeBuilder(registry)
        .addProperty("a", STRING_TYPE, null)
        .build();
    assertFalse(recordA.isSubtype(recordB));
    assertFalse(recordB.isSubtype(recordA));
  }
