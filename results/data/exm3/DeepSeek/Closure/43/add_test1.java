// com/google/javascript/jscomp/TypeCheckTest.java
public void testLends13() throws Exception {
    testTypes(
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "Foo.prototype = /** @lends {Foo.prototype} */ {/** @type {number} */ bar: 1};\n" +
        "/** @return {string} */ function f() { return (new Foo()).bar; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }
