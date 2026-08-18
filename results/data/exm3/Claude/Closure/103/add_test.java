// com/google/javascript/jscomp/CheckUnreachableCodeTest.java
public void testInstanceOfThrowsExceptionNoTry() {
    testSame("function f() { if (value instanceof type) return true; }");
  }