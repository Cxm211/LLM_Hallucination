  public void testEnum17() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }