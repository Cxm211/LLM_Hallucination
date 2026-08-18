// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceExtendsNonExistentWithValidInterface() throws Exception {
    String js = "/** @interface */function ValidInterface() {}\n" +
        "/** @interface \n" +
        " * @extends {ValidInterface} \n" +
        " * @extends {nonExistent} \n" +
        " */function A() {}";
    String[] expectedWarnings = {
      "Bad type annotation. Unknown type nonExistent"
    };
    testTypes(js, expectedWarnings);
  }