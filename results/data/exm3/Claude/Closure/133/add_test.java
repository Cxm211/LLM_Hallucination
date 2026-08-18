// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testTextExtentsMultiLine() {
  parse("@param {string} x Some text\n *    continued on next line */",
      true, "Bad type annotation. type not recognized due to syntax error");
}