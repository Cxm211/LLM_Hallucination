  public void testEnum1() throws Exception {
    testTypes("var a={BB:1,CC:2};\n" +
        "var d;d=a.BB;");
  }