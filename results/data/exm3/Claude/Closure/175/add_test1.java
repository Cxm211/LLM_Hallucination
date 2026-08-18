// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testNoInlineMutableArgsWithSideEffects() {
    testSame("function foo(x, y){return x + y;}foo(obj.prop, sideEffectCall());");
  }