  public void testBadExtends3() throws Exception {
    testTypes("function base() {}",
        "@extends used without @constructor or @interface for base");
  }