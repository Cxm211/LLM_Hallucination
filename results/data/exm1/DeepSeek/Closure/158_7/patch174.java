  public void testAbstractMethodHandling1() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        "abstractFn(1);");
  }