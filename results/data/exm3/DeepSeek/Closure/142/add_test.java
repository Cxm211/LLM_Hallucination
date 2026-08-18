// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testParseLicenseWithMultipleAnnotations() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo \n * @author Bar \n * @version 1.0 */";
    parse(comment);
    assertEquals(" Foo \n @author Bar \n @version 1.0 ",
        node.getJSDocInfo().getLicense());
  }
