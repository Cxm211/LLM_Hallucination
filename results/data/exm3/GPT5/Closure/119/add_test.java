// com/google/javascript/jscomp/CheckGlobalNamesTest.java::testGlobalCatchShadowGlobalVar
public void testGlobalCatchShadowGlobalVar() throws Exception {
    testSame(
        "var e = 0;" +
        "try {" +
        "  throw 1;" +
        "} catch (e) {" +
        "  e++;" +
        "}");
  }