// com/google/javascript/rhino/jstype/UnionTypeTest.java::testGreatestSubtypeUnionTypes5_reverse
public void testGreatestSubtypeUnionTypes5_reverse() throws Exception {
    JSType errUnion = createUnionType(EVAL_ERROR_TYPE, URI_ERROR_TYPE);
    assertEquals(NO_OBJECT_TYPE,
        STRING_OBJECT_TYPE.getGreatestSubtype(errUnion));
  }