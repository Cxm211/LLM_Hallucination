  public void testInstanceof5() throws Exception {
    
    testTypes(" function foo(){" +
        "if (foo() instanceof Object)return;}");
  }