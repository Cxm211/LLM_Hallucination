// com/google/javascript/jscomp/ScopedAliasesTest.java
public void testIssue1103d() {
    test("goog.scope(function () {" +
         "  var x, y;" +
         "});",
         SCOPE_NAMESPACE + "");
  }
