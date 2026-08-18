// com/google/javascript/jscomp/InlineObjectLiteralsTest.java
public void testEmptyObjectLiteralWithSingleProperty() {
    testLocal("var a = {x: 1}; a = {}", "var x = 1; true");
  }