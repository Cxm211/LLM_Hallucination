// com/google/javascript/jscomp/TypeCheckTest.java
public void testTemplateInferenceWithUnrelatedTemplateParam() throws Exception {
  testTypes(
      "/**\n" +
      "  * @template T\n" +
      "  * @constructor\n" +
      "  */\n" +
      "function Box() {}\n" +
      "\n" +
      "/**\n" +
      "  * @template U\n" +
      "  * @param {U} value\n" +
      "  * @return {U}\n" +
      "  */\n" +
      "Box.prototype.wrap = function(value) {};\n" +
      "\n" +
      "/** @type {number} */ var result = new Box().wrap('string');",
      "initializing variable\n" +
      "found   : string\n" +
      "required: number");
}