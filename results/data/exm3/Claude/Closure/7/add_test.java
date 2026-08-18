// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java
public void testGoogIsFunction3() throws Exception {
  testClosureFunction("goog.isFunction",
      createNullableType(U2U_CONSTRUCTOR_TYPE),
      U2U_CONSTRUCTOR_TYPE,
      NULL_TYPE);
}