  public void testEnum3() throws Exception {
    testTypes("var a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }