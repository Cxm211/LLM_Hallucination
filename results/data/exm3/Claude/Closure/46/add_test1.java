// com/google/javascript/rhino/jstype/RecordTypeTest.java
public void testSupAndInf_NoCommonProperties() {
    JSType recordA = new RecordTypeBuilder(registry)
        .addProperty("a", NUMBER_TYPE, null)
        .build();
    JSType recordB = new RecordTypeBuilder(registry)
        .addProperty("b", NUMBER_TYPE, null)
        .build();

    JSType aSupB = registry.createUnionType(recordA, recordB);

    Asserts.assertTypeEquals(
        aSupB, recordA.getLeastSupertype(recordB));
  }