// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testParseLicenseLeadingSpaces() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license First\n   Second\n  Third*/";
    parse(comment);
    assertEquals(" First\n   Second\n  Third", node.getJSDocInfo().getLicense());
  }
