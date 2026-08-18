// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testUselessCodeInCommaFirstChild() {
  test("var a = (1, bar());", "var a = (JSCOMPILER_PRESERVE(1), bar());", e);
  test("var a = (foo(), 2, bar());", "var a = (foo(), JSCOMPILER_PRESERVE(2), bar());", e);
}