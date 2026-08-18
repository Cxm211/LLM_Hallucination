// com/google/javascript/rhino/jstype/UnionTypeTest.java
public void testGreatestSubtypeUnionTypes6() throws Exception {
    JSType union = createUnionType(DATE_TYPE, REGEXP_TYPE);
    assertEquals(NO_OBJECT_TYPE,
        union.getGreatestSubtype(STRING_OBJECT_TYPE));
  }
