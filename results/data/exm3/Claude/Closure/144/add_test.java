// com/google/javascript/jscomp/CodePrinterTest.java
public void testReturnTypeNonConstructorFunction() {
  assertTypeAnnotations(
      "function foo() { return; }",
      "/**\n * @return {undefined}\n */\nfunction foo() {\n  return\n}");
}