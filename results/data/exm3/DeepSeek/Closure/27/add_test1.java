// com/google/javascript/rhino/IRTest.java
public void testTryFinallyWithLabelsThrows() {
    try {
      IR.tryFinally(IR.label("foo"), IR.label("bar"));
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      // expected
    }
  }
