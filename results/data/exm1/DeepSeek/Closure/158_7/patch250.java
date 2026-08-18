  public void testEnum14() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.FOO;");
  }