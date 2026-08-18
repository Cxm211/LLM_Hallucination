// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testMultipleUninitializedVars() {
    test("goog.scope(function () {" +
         "  var x, y, z;" +
         "  foo.bar = function () { x = 1; y = 2; z = 3; };" +
         "};",
         SCOPE_NAMESPACE + "foo.bar = function () { $jscomp.scope.x = 1; $jscomp.scope.y = 2; $jscomp.scope.z = 3; }");
  }