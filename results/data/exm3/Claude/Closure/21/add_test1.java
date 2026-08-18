// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testUselessCodeInCommaFirstChild() {
  test("var a; (1, 2, 3);",
       "var a; (JSCOMPILER_PRESERVE(1), JSCOMPILER_PRESERVE(2), 3);", e);
}