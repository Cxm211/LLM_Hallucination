// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue586_2() throws Exception {
    testTypes(
        "/** @constructor */" +
        "var MyClass2 = function() {};" +
        "/** @param {string} s @param {number} n */" +
        "MyClass2.prototype.fn2 = function(s, n) {};" +
        "MyClass2.prototype.test = function() {" +
        "  this.fn2(42);" +
        "  this.fn2 = function() {};" +
        "};,
        "Function MyClass2.prototype.fn2: called with 1 argument(s). " +
        "Function requires at least 2 argument(s) " +
        "and no more than 2 argument(s).");
  }
