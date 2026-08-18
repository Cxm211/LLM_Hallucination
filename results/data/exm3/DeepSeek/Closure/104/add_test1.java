// com/google/javascript/rhino/jstype/UnionTypeTest.java
public void testGreatestSubtypeUnionTypes7() throws Exception {
    JSType union = createUnionType(DATE_TYPE, REGEXP_TYPE, STRING_OBJECT_TYPE);
    assertEquals(NO_OBJECT_TYPE,
        union.getGreatestSubtype(BOOLEAN_OBJECT_TYPE));
  }
