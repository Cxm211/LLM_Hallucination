// com/google/javascript/jscomp/PeepholeReplaceKnownMethodsTest.java
public void testNoStringJoinWithStringLiteralExtraArg() {
    foldSame("x = ['a', 'b', 'c'].join('-', 'extra')");
  }