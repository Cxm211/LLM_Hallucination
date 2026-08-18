  public void testScoping7() throws Exception {
    testTypes("function A() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of A\n" +
        "found   : null\n" +
        "required: A");
  }