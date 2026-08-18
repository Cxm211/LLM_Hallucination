  public void testEnum8() throws Exception {
    testTypes("var a=8;",
        "enum initializer must be an object literal or an enum");
  }