  public void testAdd1() throws Exception {
    testTypes("function foo(){var a = 'abc'+foo();}");
  }