  public void testEnum15() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.BAR;",
        "element BAR does not exist on this enum");
  }