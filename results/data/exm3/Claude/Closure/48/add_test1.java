// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue586_AdditionalTest2() throws Exception {
    testTypes(
        "/** @constructor */" +
        "var MyClass = function() {};" +
        "/** @return {number} */" +
        "MyClass.prototype.getValue = function() { return 42; };" +
        "MyClass.prototype.test = function() {" +
        "  var x = this.getValue();" +
        "  this.getValue = function() {};" +
        "  var y = this.getValue();" +
        "};");
  }