// com/google/javascript/jscomp/TypeCheckTest.java
public void testTemplateInferenceWithNoMatchingParameters() throws Exception {
  testTypes(
      "/**\n" +
      "  * @constructor\n" +
      "  * @template T\n" +
      "  */\n" +
      "function Container() {}\n" +
      "\n" +
      "/**\n" +
      "  * @return {T}\n" +
      "  */\n" +
      "Container.prototype.get = function() {};\n" +
      "\n" +
      "/** @type {string} */ var x = new Container().get();");
}