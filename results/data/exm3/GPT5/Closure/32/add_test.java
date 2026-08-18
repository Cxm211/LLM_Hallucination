// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java::testParseLicenseNoSpaceAfterStar
public void testParseLicenseNoSpaceAfterStar() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo\n *Bar*/";
    parse(comment);
    assertEquals(" Foo\nBar", node.getJSDocInfo().getLicense());
  }