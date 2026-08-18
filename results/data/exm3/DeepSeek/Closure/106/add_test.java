// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testNormalCommentSideEffect() {
    test("function f() { /* comment */ alert(1); }", e);
  }
