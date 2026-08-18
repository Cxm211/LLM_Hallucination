// com/google/javascript/rhino/jstype/JSTypeTest.java
public void testRecordTypeLeastSuperType4() {
    RecordTypeBuilder builder1 = new RecordTypeBuilder(registry);
    builder1.addProperty("x", NUMBER_TYPE, null);
    JSType record1 = builder1.build();
    RecordTypeBuilder builder2 = new RecordTypeBuilder(registry);
    builder2.addProperty("y", STRING_TYPE, null);
    JSType record2 = builder2.build();
    assertTypeEquals(
        registry.createUnionType(record1, record2),
        record1.getLeastSupertype(record2));
  }
