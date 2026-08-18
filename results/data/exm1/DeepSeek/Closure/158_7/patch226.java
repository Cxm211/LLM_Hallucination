  public void testComparison2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "if (a!==b) {}",
        "condition always evaluates to the same value\n" +
        "left : number\n" +
        "right: Date");
  }