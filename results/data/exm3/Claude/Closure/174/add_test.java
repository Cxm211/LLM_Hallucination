// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testUninitializedVarWithMultipleDeclarations() {
    test("goog.scope(function () {" +
         "  var a, b = foo.bar;" +
         "};",
         SCOPE_NAMESPACE + "$jscomp.scope.b=foo.bar");
  }