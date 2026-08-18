// com/google/javascript/jscomp/NameAnalyzerTest.java
public void testAssignWithCallNested() {
  test("var fun, x; var wrapper = (fun = function(){ x; })();",
      "var x; var wrapper = (function(){ x; })();");
}