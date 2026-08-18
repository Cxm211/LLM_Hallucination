// com/google/javascript/jscomp/PeepholeReplaceKnownMethodsTest.java
public void testNoFoldArrayJoinWithExtraArgs() {
  foldSame("x = [].join(',', 2)");
  foldSame("x = [1].join(',', 2)");
  foldSame("x = [1,2].join(',', 2)");
  foldSame("x = ['a','b'].join('', 3)");
  foldSame("x = [foo].join(null, bar)");
}
