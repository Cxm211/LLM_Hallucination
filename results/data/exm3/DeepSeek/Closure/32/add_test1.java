// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testParsePreserveMixedStars() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@preserve\n * line1\n   line2*/";
    parse(comment);
    assertEquals("\n  line1\n   line2", node.getJSDocInfo().getLicense());
  }
