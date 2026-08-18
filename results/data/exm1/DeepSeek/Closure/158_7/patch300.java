  public void testBadExtends1() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n",
        "Bad type annotation. Unknown type not_base");
  }