// com/google/javascript/jscomp/InlineVariablesTest.java
public void testAssignmentInFunctionCalledMultipleTimes() {
    testSame(
        "var w; function g() { w = getValue(); } g(); g(); alert(w);");
  }