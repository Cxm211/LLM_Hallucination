// com/google/javascript/jscomp/TypeCheckTest.java::testIssue586_constructorOverride
public void testIssue586_constructorOverride() throws Exception {
    testTypes(
        "/** @constructor */" +
        "var MyClass = function() { this.fn = function() {}; };" +
        "/** @param {boolean} success */" +
        "MyClass.prototype.fn = function(success) {};" +
        "MyClass.prototype.test = function() {" +
        "  this.fn();" +
        "};",
        "Function MyClass.prototype.fn: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }