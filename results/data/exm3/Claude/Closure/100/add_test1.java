// com/google/javascript/jscomp/CheckGlobalThisTest.java
public void testInnerFunctionThisWithoutPropertyAccess() {
    testSame("function f() { function g() { return this; } }");
  }