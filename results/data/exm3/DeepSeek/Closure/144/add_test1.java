// com/google/javascript/jscomp/CodePrinterTest.java
public void testNoJSDocFunctionExpression() {
    assertTypeAnnotations(
        "var g = function() {};",
        "/**\n * @return {undefined}\n */\nvar g = function() {\n};");
  }
