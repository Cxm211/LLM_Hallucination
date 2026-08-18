// com/google/javascript/rhino/jstype/FunctionTypeTest.java
public void testUnionWithLeastFunctionType() {
    JSType least = LEAST_FUNCTION_TYPE;
    JSType other = getNativeType("Function");
    JSType union = least.getLeastSupertype(other);
    assertEquals(other, union);
  }
