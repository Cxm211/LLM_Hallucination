// com/google/javascript/rhino/jstype/UnionTypeTest.java
public void testGreatestSubtypeUnionTypes7() throws Exception {
    JSType numOrObj = createUnionType(NUMBER_TYPE, OBJECT_TYPE);
    JSType strOrBool = createUnionType(STRING_TYPE, BOOLEAN_TYPE);
    assertEquals(NO_TYPE, numOrObj.getGreatestSubtype(strOrBool));
  }