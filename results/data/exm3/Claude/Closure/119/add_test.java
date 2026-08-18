// com/google/javascript/jscomp/CheckGlobalNamesTest.java
public void testGlobalCatchWithVar() throws Exception {
  testSame(
      "var e = 10;" +
      "try {" +
      "  throw Error();" +
      "} catch (e) {" +
      "  console.log(e)" +
      "}");
}