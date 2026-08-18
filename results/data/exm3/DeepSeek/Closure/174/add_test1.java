// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testIssue1103e() {
    test("goog.scope(function () {" +
         "  /** @type {!Array<string>} */ var arr;" +
         "});",
         SCOPE_NAMESPACE + "/** @type {!Array<string>} */ $jscomp.scope.arr;");
  }
