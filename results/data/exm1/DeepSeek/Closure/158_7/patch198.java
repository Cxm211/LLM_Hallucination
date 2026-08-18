  public void testDuplicateStaticPropertyDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }