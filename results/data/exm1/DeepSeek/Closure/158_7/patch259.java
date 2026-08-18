  public void testEnum23() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<number>\n" +
        "required: string");
  }