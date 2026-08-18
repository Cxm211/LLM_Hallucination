// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testUselessCodeInNestedComma() {
  test("var a = ((1, 2), bar());", "var a = ((JSCOMPILER_PRESERVE(1), 2), bar());", e);
  test("var a = (foo(), (3, 4));", "var a = (foo(), (JSCOMPILER_PRESERVE(3), 4));", e);
}