// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testUselessCodeInNestedComma() {
  test("var a; a = (1, (2, 3));",
       "var a; a = (JSCOMPILER_PRESERVE(1), (JSCOMPILER_PRESERVE(2), 3));", e);
}