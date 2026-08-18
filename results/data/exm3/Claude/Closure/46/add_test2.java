// com/google/javascript/rhino/jstype/JSTypeTest.java
public void testRecordTypeLeastSuperType_PartialOverlap() {
    RecordTypeBuilder builder1 = new RecordTypeBuilder(registry);
    builder1.addProperty("a", NUMBER_TYPE, null);
    builder1.addProperty("b", STRING_TYPE, null);
    builder1.addProperty("c", NUMBER_TYPE, null);
    JSType recordType1 = builder1.build();

    RecordTypeBuilder builder2 = new RecordTypeBuilder(registry);
    builder2.addProperty("b", STRING_TYPE, null);
    builder2.addProperty("d", NUMBER_TYPE, null);
    JSType recordType2 = builder2.build();

    RecordTypeBuilder expectedBuilder = new RecordTypeBuilder(registry);
    expectedBuilder.addProperty("b", STRING_TYPE, null);
    JSType expectedType = expectedBuilder.build();

    assertTypeEquals(
        expectedType,
        recordType1.getLeastSupertype(recordType2));
  }