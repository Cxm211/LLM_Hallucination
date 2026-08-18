// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java::testParseLicenseWithAnnotation
public void testParseLicenseWithMultipleAnnotations() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo \n * @bar Baz\n * @author Alice */";
    parse(comment);
    assertEquals(" Foo \n @bar Baz\n @author Alice ",
        node.getJSDocInfo().getLicense());
  }