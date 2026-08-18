// com/google/javascript/jscomp/CrossModuleMethodMotionTest.java
public void testIssue600f() {
    testSame(
        createModuleChain(
            "var Foo = function() {};\n" +
            "(function() {\n" +
            "  var outer = 5;\n" +
            "  Foo.prototype.bar = function() {\n" +
            "    return outer;\n" +
            "  };\n" +
            "})();",

            "(function() {" +
            "  var obj = new Foo();" +
            "  obj.bar();" +
            "})();"));
  }
