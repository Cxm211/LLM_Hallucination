  public void testEnum5() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: (String|null|undefined)");
  }