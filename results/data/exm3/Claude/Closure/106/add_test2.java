// com/google/javascript/jscomp/CheckSideEffectsTest.java
public void testJSDocBlockComment() {
  test("function A() { /** @type {number} */ this.foo = 1; }", ok);
}