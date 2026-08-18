  public void testEnum31() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: A.<number>");
  }