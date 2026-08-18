// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testUninitializedVarWithJSDocAndLaterAssignment() {
    test("goog.scope(function () {" +
         "  /** @type {string} */ var str;" +
         "  str = 'hello';" +
         "};",
         SCOPE_NAMESPACE + "/** @type {string} */ $jscomp.scope.str; $jscomp.scope.str = 'hello'");
  }