// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testSideEffectsNoParamUse() {
    test(
        "function f(a) { return alert('hi'); } f(x);",
        "alert('hi');");
  }
