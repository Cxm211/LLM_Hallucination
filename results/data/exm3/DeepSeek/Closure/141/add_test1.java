// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testDecomposeNamedFunctionExpressionInCall() {
    test(
        "(function(map){descriptions_=map})(\n" +
           "function myFunc(){\n" +
              "var ret={};\n" +
              "ret[ONE]='a';\n" +
              "ret[TWO]='b';\n" +
              "return ret\n" +
           "}()\n" +
        ");",
        "{" +
        "var JSCompiler_inline_result_0;" +
        "var ret$$inline_2={};\n" +
        "ret$$inline_2[ONE]='a';\n" +
        "ret$$inline_2[TWO]='b';\n" +
        "JSCompiler_inline_result_0 = ret$$inline_2;\n" +
        "}" +
        "{" +
        "descriptions_=JSCompiler_inline_result_0;" +
        "}"
        );
  }
