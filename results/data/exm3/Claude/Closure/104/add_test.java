// com/google/javascript/rhino/jstype/UnionTypeTest.java
public void testGreatestSubtypeUnionTypes6() throws Exception {
    JSType numOrStr = createUnionType(NUMBER_TYPE, STRING_TYPE);
    JSType boolOrNull = createUnionType(BOOLEAN_TYPE, NULL_TYPE);
    assertEquals(NO_TYPE, numOrStr.getGreatestSubtype(boolOrNull));
  }