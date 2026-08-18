// com/google/javascript/jscomp/PeepholeReplaceKnownMethodsTest.java
public void testNoStringJoinMultipleExtraArgs() {
    foldSame("x = ['a', 'b'].join(',', 1, 2)");
    foldSame("x = [1, 2, 3].join('', foo, bar)");
  }