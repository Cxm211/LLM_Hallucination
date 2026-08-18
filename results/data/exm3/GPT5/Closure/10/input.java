// buggy function
  static boolean mayBeString(Node n, boolean recurse) {
    if (recurse) {
      return allResultsMatch(n, MAY_BE_STRING_PREDICATE);
    } else {
      return mayBeStringHelper(n);
    }
  }

// trigger testcase
// com/google/javascript/jscomp/PeepholeFoldConstantsTest.java::testIssue821
public void testIssue821() {
    foldSame("var a =(Math.random()>0.5? '1' : 2 ) + 3 + 4;");
    foldSame("var a = ((Math.random() ? 0 : 1) ||" +
             "(Math.random()>0.5? '1' : 2 )) + 3 + 4;");
  }
