// com/google/javascript/jscomp/FunctionRewriterTest.java::testIssue538
public void testIssue538_setter() {
    checkCompilesToSame(
        "/** @constructor */\n" +
        "WebInspector.Setting = function() {}\n" +
        "WebInspector.Setting.prototype = {\n" +
        "    set name0(x){this._name = x;},\n" +
        "    set name1(x){this._name = x;},\n" +
        "    set name2(x){this._name = x;},\n" +
        "    set name3(x){this._name = x;},\n" +
        "    set name4(x){this._name = x;},\n" +
        "    set name5(x){this._name = x;},\n" +
        "    set name6(x){this._name = x;},\n" +
        "    set name7(x){this._name = x;},\n" +
        "    set name8(x){this._name = x;},\n" +
        "    set name9(x){this._name = x;},\n" +
        "}",
        1);
  }