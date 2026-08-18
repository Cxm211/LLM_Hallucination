// com/google/javascript/jscomp/CodePrinterTest.java
public void testReturnTypeArrowFunctionNoReturn() {
  assertTypeAnnotations(
      "var a = function() { var x = 1; }",
      "/**\n * @return {undefined}\n */\nvar a = function() {\n  var x = 1\n}");
}