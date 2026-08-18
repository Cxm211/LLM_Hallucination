// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineWithFewerArgsThanParams() {
    // Function has 2 params but called with 1 arg - should still inline
    test("function f(a, b) { return a; } f(1);", "1;");
  }