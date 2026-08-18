  public void testBooleanReduction4() throws Exception {
    testTypes("" +
        "(function(x) { return null || x || null ; })");
  }