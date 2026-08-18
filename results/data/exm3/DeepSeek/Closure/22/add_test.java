// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testUselessCodeInForInit() {
    test("for ((1,2);;);",
         "for ((JSCOMPILER_PRESERVE(1),JSCOMPILER_PRESERVE(2));;);", e);
}
