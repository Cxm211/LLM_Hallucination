  public void testTypeOfReduction13() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return goog.isObject(x) ? x : []; }", null);
  }