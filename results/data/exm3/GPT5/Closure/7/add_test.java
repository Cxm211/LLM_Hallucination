// com/google/javascript/jscomp/ClosureReverseAbstractInterpreterTest.java::testGoogIsFunction_Object
public void testGoogIsFunction_Object() throws Exception {
  testClosureFunction("goog.isFunction",
      OBJECT_TYPE,
      U2U_CONSTRUCTOR_TYPE,
      OBJECT_TYPE);
}