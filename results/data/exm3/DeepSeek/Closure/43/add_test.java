// com/google/javascript/jscomp/TypeCheckTest.java
public void testLends12() throws Exception {
    testTypes(
        "/** @constructor */\n" +
        "var Foo = /** @lends {Foo.prototype} */ {/** @type {number} */ bar: 1};\n" +
        "/** @return {string} */ function f() { return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }
