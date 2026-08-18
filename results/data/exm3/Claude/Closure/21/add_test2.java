// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testUselessCodeInForLoopComma() {
  test("for((1, 2);;){};",
       "for((JSCOMPILER_PRESERVE(1), 2);;){};", e);
}