  public void testAdd8() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }