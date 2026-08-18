  public void testAbstractMethodHandling3() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        "goog.abstractFn(1);");
  }