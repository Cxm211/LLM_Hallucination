// com/google/javascript/rhino/jstype/JSTypeTest.java::testRecordTypeLeastSuperType2
public void testRecordTypeLeastSuperTypeOverlap() {
    RecordTypeBuilder b1 = new RecordTypeBuilder(registry);
    b1.addProperty("a", NUMBER_TYPE, null);
    b1.addProperty("b", STRING_TYPE, null);
    JSType r1 = b1.build();

    RecordTypeBuilder b2 = new RecordTypeBuilder(registry);
    b2.addProperty("b", STRING_TYPE, null);
    b2.addProperty("c", NUMBER_TYPE, null);
    JSType r2 = b2.build();

    assertTypeEquals(
        registry.createUnionType(r1, r2),
        r1.getLeastSupertype(r2));
  }