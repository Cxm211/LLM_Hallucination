// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue700_AdditionalCase1() throws Exception {
  testTypes(
      "/**\n" +
      " * @param {{x: number}} opt_data\n" +
      " * @return {number}\n" +
      " */\n" +
      "function temp1(opt_data) {\n" +
      "  return opt_data.x;\n" +
      "}\n" +
      "\n" +
      "/**\n" +
      " * @param {{value: (boolean|string)}} opt_data\n" +
      " * @return {number}\n" +
      " */\n" +
      "function temp2(opt_data) {\n" +
      "  /** @notypecheck */\n" +
      "  function __inner() {\n" +
      "    return temp1(opt_data.value);\n" +
      "  }\n" +
      "  return __inner();\n" +
      "}\n" +
      "\n" +
      "function callee() {\n" +
      "  var output = temp2({\n" +
      "    value: 'test'\n" +
      "  })\n" +
      "  alert(output);\n" +
      "}\n" +
      "\n" +
      "callee();");
}