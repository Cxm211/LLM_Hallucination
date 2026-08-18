  public void testDuplicateLocalVarDecl() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {  var x = ''; }",
        "variable x redefined with type string, " +
        "original definition at [testcode]:2 with type number");
  }