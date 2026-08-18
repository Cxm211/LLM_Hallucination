// com/google/javascript/rhino/jstype/JSTypeTest.java
public void testRecordTypeLeastSuperType_NoCommonProperties() {
    RecordTypeBuilder builder1 = new RecordTypeBuilder(registry);
    builder1.addProperty("a", NUMBER_TYPE, null);
    builder1.addProperty("b", STRING_TYPE, null);
    JSType recordType1 = builder1.build();

    RecordTypeBuilder builder2 = new RecordTypeBuilder(registry);
    builder2.addProperty("x", NUMBER_TYPE, null);
    builder2.addProperty("y", STRING_TYPE, null);
    JSType recordType2 = builder2.build();

    assertTypeEquals(
        registry.createUnionType(recordType1, recordType2),
        recordType1.getLeastSupertype(recordType2));
  }