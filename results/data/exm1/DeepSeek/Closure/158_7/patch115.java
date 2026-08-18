  public void testInstanceof6() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }