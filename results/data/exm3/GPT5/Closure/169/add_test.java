// com/google/javascript/rhino/jstype/RecordTypeTest.java::testSubtypeWithUnknowns2
public void testFunctionParamEquivalenceAsymmetry() throws Exception {
    FunctionType fnWithNumberParam = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(UNKNOWN_TYPE)
        .build();
    FunctionType fnWithUnknownParam = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters((JSType) null))
        .withReturnType(UNKNOWN_TYPE)
        .build();
    assertFalse(fnWithNumberParam.hasEqualCallType(fnWithUnknownParam));
    assertFalse(fnWithUnknownParam.hasEqualCallType(fnWithNumberParam));
  }