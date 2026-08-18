// com/google/javascript/jscomp/TypeCheckTest.java
public void testMultipleNonExistentInterfacesInChain() throws Exception {
    String js = "/** @interface \n" +
        " * @extends {nonExistent1} \n" +
        " */function A() {}\n" +
        "/** @interface \n" +
        " * @extends {A} \n" +
        " * @extends {nonExistent2} \n" +
        " */function B() {}";
    String[] expectedWarnings = {
      "Bad type annotation. Unknown type nonExistent1",
      "Bad type annotation. Unknown type nonExistent2"
    };
    testTypes(js, expectedWarnings);
  }