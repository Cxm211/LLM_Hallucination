// com/google/javascript/jscomp/CheckGlobalNamesTest.java
public void testGlobalCatchReference() throws Exception {
    testSame(
        "try {" +
        "  throw Error();" +
        "} catch (e) {" +
        "  console.log(e);" +
        "}");
  }
