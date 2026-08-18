  public void testDuplicateStaticPropertyDecl9() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function EventCopy() {}");
  }