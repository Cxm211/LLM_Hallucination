// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testRecordTypeLeastSuperTypeDifferentPropType() {
    JSType recordA = new RecordTypeBuilder(registry)
        .addProperty("a", NUMBER_TYPE, null)
        .build();
    JSType recordB = new RecordTypeBuilder(registry)
        .addProperty("a", STRING_TYPE, null)
        .build();
    JSType union = registry.createUnionType(recordA, recordB);
    Asserts.assertTypeEquals(union, recordA.getLeastSupertype(recordB));
  }
