// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineWithMoreArgsThanParams() {
    // Function has 1 param but called with 2 args - should still inline if no side effects
    test("function f(a) { return a; } f(1, 2);", "1;");
  }