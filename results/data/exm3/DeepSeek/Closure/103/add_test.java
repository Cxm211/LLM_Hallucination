// com/google/javascript/jscomp/CheckUnreachableCodeTest.java
public void testInstanceOfInAssignment() {
    testSame("function f() {try { var a = b instanceof c; } catch (e) {}}");
  }
