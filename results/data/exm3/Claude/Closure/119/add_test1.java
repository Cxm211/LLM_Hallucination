// com/google/javascript/jscomp/CheckGlobalNamesTest.java
public void testNestedCatchBlocks() throws Exception {
  testSame(
      "try {" +
      "  try {" +
      "    throw Error();" +
      "  } catch (inner) {" +
      "    console.log(inner);" +
      "  }" +
      "} catch (outer) {" +
      "  console.log(outer);" +
      "}");
}