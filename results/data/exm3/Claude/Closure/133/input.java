// buggy function
  private String getRemainingJSDocLine() {
    String result = stream.getRemainingJSDocLine();
    return result;
  }

// trigger testcase
// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java::testTextExtents
public void testTextExtents() {
    parse("@return {@code foo} bar \n *    baz. */",
        true, "Bad type annotation. type not recognized due to syntax error");
  }
