// com/google/javascript/rhino/jstype/FunctionTypeTest.java
public void testEmptyFunctionTypesWithSubtypes() {
  JSType leastFunctionSubtype = registry.createFunctionType(
      registry.getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE).toMaybeFunctionType());
  assertTrue(leastFunctionSubtype.isEmptyType());
  
  JSType normalFunction = registry.createFunctionType(
      registry.getNativeType(JSTypeNative.NUMBER_TYPE));
  assertFalse(normalFunction.isEmptyType());
}