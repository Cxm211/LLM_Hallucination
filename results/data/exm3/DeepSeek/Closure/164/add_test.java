// com/google/javascript/rhino/jstype/FunctionTypeTest.java
public void testIsSubtypeWithOneParamVsZero() {
    FunctionType oneParam = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();
    FunctionType zeroParams = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters())
        .withReturnType(BOOLEAN_TYPE).build();
    assertFalse(oneParam.isSubtype(zeroParams));
  }
