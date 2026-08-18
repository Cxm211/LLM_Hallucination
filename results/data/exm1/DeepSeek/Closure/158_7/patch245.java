  public void testEnum9() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.a=8;",
        "enum initializer must be an object literal or an enum");
  }