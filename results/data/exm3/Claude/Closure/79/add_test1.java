// com/google/javascript/jscomp/VarCheckTest.java
public void testMultipleExternReferences() {
  testSame("foo(); bar();", "function foo() {} function bar() {}",
      VarCheck.NAME_REFERENCE_IN_EXTERNS_ERROR);
}