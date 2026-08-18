// com/google/javascript/jscomp/FunctionRewriterTest.java
public void testSetterNotReduced() {
    checkCompilesToSame(
        "/** @constructor */\n" +
        "WebInspector.Setting = function() {}\n" +
        "WebInspector.Setting.prototype = {\n" +
        "    set name(x){this._name = x;}\n" +
        "}", 1);
  }
