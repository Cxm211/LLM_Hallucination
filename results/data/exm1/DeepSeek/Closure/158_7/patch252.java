  public void testEnum16() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }