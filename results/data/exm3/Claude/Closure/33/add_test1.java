// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue700_AdditionalCase2() throws Exception {
  testTypes(
      "/**\n" +
      " * @param {{a: string, b: number}} opt_data\n" +
      " * @return {string}\n" +
      " */\n" +
      "function temp1(opt_data) {\n" +
      "  return opt_data.a + opt_data.b;\n" +
      "}\n" +
      "\n" +
      "/**\n" +
      " * @param {{nested: (null|Object)}} opt_data\n" +
      " * @return {string}\n" +
      " */\n" +
      "function temp2(opt_data) {\n" +
      "  /** @notypecheck */\n" +
      "  function __inner() {\n" +
      "    return temp1(opt_data.nested);\n" +
      "  }\n" +
      "  return __inner();\n" +
      "}\n" +
      "\n" +
      "function callee() {\n" +
      "  var output = temp2({\n" +
      "    nested: null\n" +
      "  })\n" +
      "  alert(output);\n" +
      "}\n" +
      "\n" +
      "callee();");
}