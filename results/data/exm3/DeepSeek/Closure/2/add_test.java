// com/google/javascript/jscomp/TypeCheckTest.java
public void testInterfaceExtendsNonExistentAndExisting() throws Exception {
    String js = "/** @interface */ function Exist() {};\n" +
        "/** @interface \n" +
        " * @extends {nonExistent}\n" +
        " * @extends {Exist}\n" +
        " */ function A() {};\n" +
        "/** @type {number} */ A.prototype.foo;";
    String[] expectedWarnings = {
      "Bad type annotation. Unknown type nonExistent"
    };
    testTypes(js, expectedWarnings);
  }
