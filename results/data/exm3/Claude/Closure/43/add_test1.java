// com/google/javascript/jscomp/TypeCheckTest.java
public void testLends13() throws Exception {
  testTypes(
      "function defineClass(x, y) { return function() {}; } " +
      "/** @constructor */" +
      "var Base = function() {};" +
      "Base.prototype.method = function() { return 'base'; };" +
      "/**\n" +
      " * @constructor\n" +
      " * @extends {Base}\n" +
      " */\n" +
      "var Derived = defineClass(Base, " +
      "    /** @lends {Derived.prototype} */ ({" +
      "      /** @type {number} */ prop: 42," +
      "      /** @return {string} */ method: function() { return 'derived'; }}));" +
      "/** @return {boolean} */ function f() { return (new Derived()).prop; }",
      "inconsistent return type\n" +
      "found   : number\n" +
      "required: boolean");
}