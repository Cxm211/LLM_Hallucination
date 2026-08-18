// com/google/javascript/jscomp/TypeCheckTest.java
public void testPrototypeInferenceNonConstructor() throws Exception {
  testTypes(
      "/** @param {Object} a */\n" +
      "function f(a) {\n" +
      "  a.prototype = {foo: 3};\n" +
      "}\n");
  testTypes(
      "/** @param {Object} b */\n" +
      "function g(b) {\n" +
      "  b.prototype = 5;\n" +
      "}\n");
  testTypes(
      "/** @param {Object} c */\n" +
      "function h(c) {\n" +
      "  c.prototype = 'str';\n" +
      "}\n");
}
