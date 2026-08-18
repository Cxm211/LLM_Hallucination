  public void testFunctionArguments10() throws Exception {
    
    testTypes(
        "function a(a,opt_a,b,c) {}",
        "optional arguments must be at the end");
  }