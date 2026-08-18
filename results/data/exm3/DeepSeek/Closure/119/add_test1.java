// com/google/javascript/jscomp/CheckGlobalNamesTest.java
public void testGlobalCatchNestedFunction() throws Exception {
    testSame(
        "try {" +
        "  throw Error();" +
        "} catch (e) {" +
        "  function f() { return e; }" +
        "}");
  }
