// com/google/javascript/jscomp/NameAnalyzerTest.java
public void testAssignWithCallNested() {
    test("var fun, x; ((fun = function(){ x; }))();",
        "var x; (function(){ x; })();");
  }
