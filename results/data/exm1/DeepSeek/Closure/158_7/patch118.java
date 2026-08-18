  public void testScoping2() throws Exception {
    testTypes(
        " var a;" +
        "function Foo() {" +
        "   var a;" +
        "}");
  }