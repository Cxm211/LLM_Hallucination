// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testNoInlineExtraArgWithSideEffect() {
    // Extra argument has side effect - should not inline
    testSame("function f(a) { return a; } f(x, alert('hi'));");
  }