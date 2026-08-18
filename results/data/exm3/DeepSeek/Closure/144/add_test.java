// com/google/javascript/jscomp/CodePrinterTest.java
public void testNoJSDocFunction() {
    assertTypeAnnotations(
        "function f() {}",
        "/**\n * @return {undefined}\n */\nfunction f() {\n}");
  }
