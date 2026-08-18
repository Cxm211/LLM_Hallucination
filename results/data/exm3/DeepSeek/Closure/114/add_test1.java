// com/google/javascript/jscomp/NameAnalyzerTest.java
public void testAssignWithCallArgument() {
    test("var fun, x; foo(fun = function(){ x; });",
        "var x; foo(function(){ x; });");
  }
