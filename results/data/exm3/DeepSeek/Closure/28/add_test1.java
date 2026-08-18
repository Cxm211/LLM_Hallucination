// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testInlineFunctionWithIdentifier() {
    String f = "var f = function() { return x; };";
    test(f + "if (!f()) alert('y');", "if (!x) alert('y');");
  }
