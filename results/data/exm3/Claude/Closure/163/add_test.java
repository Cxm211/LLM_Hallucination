// com/google/javascript/jscomp/CrossModuleMethodMotionTest.java
public void testIssue600_NestedClosureWithMultipleLevels() {
  testSame(
      createModuleChain(
          "var Outer = (function() {\n" +
          "  var Middle = (function() {\n" +
          "    var Inner = function() {};\n" +
          "    var capturedVar = 42;\n" +
          "    Inner.prototype = {\n" +
          "      getValue: function() {\n" +
          "        return capturedVar;\n" +
          "      }\n" +
          "    };\n" +
          "    return Inner;\n" +
          "  })();\n" +
          "  return Middle;\n" +
          "})();\n",

          "(function() {" +
          "  var obj = new Outer();" +
          "  obj.getValue();" +
          "})();"));
}