// com/google/javascript/rhino/jstype/FunctionTypeTest.java::testSubtypeRequiredParamCheck
public void testSubtypeRequiredParamCheck() {
    FunctionType twoNumbers = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE, NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();
    FunctionType oneNumber = new FunctionBuilder(registry)
        .withParamsNode(registry.createParameters(NUMBER_TYPE))
        .withReturnType(BOOLEAN_TYPE).build();

    assertFalse(twoNumbers.isSubtype(oneNumber));
    assertTrue(oneNumber.isSubtype(twoNumbers));
  }