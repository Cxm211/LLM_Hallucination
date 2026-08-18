// com/google/javascript/jscomp/TypeCheckTest.java
public void testLends12() throws Exception {
  testTypes(
      "function defineClass(x) { return function() {}; } " +
      "/** @constructor */" +
      "var Foo = defineClass(" +
      "    /** @lends {Foo.prototype} */ ({/** @type {string} */ name: 'test', /** @type {boolean} */ flag: true}));" +
      "/** @return {number} */ function f() { return (new Foo()).name; }",
      "inconsistent return type\n" +
      "found   : string\n" +
      "required: number");
}