  public void testIn4() throws Exception {
    testTypes("Date in Object",
        "left side of 'in'\n" +
        "found   : function (new:Date, ?, ?, ?, ?, ?, ?, ?): string\n" +
        "required: string");
  }