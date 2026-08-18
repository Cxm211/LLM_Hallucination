// com/google/javascript/jscomp/CheckUnreachableCodeTest.java
public void testInstanceOfNestedThrowsException() {
    testSame("function f() {try { var x = (a instanceof b) && (c instanceof d); } " +
             "catch (e) { }}");
  }