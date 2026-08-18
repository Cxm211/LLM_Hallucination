// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceExtendsChainNonExistent() throws Exception {
    String js = "/** @interface \n" +
        " * @extends {nonExistent}\n" +
        " */ function B() {};\n" +
        "/** @type {string} */ B.prototype.bar;\n" +
        "/** @interface \n" +
        " * @extends {B}\n" +
        " */ function A() {};";
    String[] expectedWarnings = {
      "Bad type annotation. Unknown type nonExistent"
    };
    testTypes(js, expectedWarnings);
  }
