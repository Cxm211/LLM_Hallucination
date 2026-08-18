// com/google/javascript/jscomp/MethodCheckTest.java
public void testExternMethodNotAdded() {
    // Extern function should not be added as a method signature
    String externs = "function externFunc() {};";
    testSame(externs, "var obj = {method: externFunc}; obj.method();", null);
  }
