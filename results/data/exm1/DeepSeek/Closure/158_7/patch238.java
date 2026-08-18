  public void testEnum2() throws Exception {
    testTypes("var a={b:1}",
        "enum key b must be a syntactic constant");
  }