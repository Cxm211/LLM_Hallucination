  public void testEnum20() throws Exception {
    testTypes(" var E = {A: 1, B: 2}; var x = []; x[E.A] = 0;");
  }