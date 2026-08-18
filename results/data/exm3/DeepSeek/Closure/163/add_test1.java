// com/google/javascript/jscomp/CrossModuleMethodMotionTest.java
public void testIssue600g() {
    testSame(
        createModuleChain(
            "var Foo = function() {};\n" +
            "(function() {\n" +
            "  var secret = 42;\n" +
            "  Foo.prototype = {\n" +
            "    method1: function() { return secret; },\n" +
            "    method2: function() { return 0; }\n" +
            "  };\n" +
            "})();",

            "(function() {" +
            "  var obj = new Foo();" +
            "  obj.method1();" +
            "  obj.method2();" +
            "})();"));
  }
