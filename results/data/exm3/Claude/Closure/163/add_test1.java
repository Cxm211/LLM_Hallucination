// com/google/javascript/jscomp/CrossModuleMethodMotionTest.java
public void testIssue600_ClosureInPrototypeAssignment() {
  testSame(
      createModuleChain(
          "var MyClass = function() {};\n" +
          "(function() {\n" +
          "  var localVar = 'test';\n" +
          "  MyClass.prototype.method = function() {\n" +
          "    return localVar;\n" +
          "  };\n" +
          "})();\n",

          "(function() {" +
          "  var instance = new MyClass();" +
          "  instance.method();" +
          "})();"));
}