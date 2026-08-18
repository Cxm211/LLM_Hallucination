// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue586_AdditionalTest1() throws Exception {
    testTypes(
        "/** @constructor */" +
        "var MyClass = function() {};" +
        "/** @param {string} msg */" +
        "MyClass.prototype.log = function(msg) {};" +
        "MyClass.prototype.test = function() {" +
        "  this.log('hello');" +
        "  this.log = function() {};" +
        "  this.log();" +
        "};");
  }