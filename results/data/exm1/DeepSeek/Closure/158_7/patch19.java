  public void testBooleanReduction1() throws Exception {
    testTypes("var x; x = null || \"a\";");
  }