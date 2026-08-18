// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue586_3() throws Exception {
    testTypes(
        "/** @constructor */" +
        "var MyClass3 = function() {};" +
        "/** @param {string} s */" +
        "MyClass3.prototype.method = function(s) {};" +
        "MyClass3.prototype.test = function() {" +
        "  var x = new MyClass3();" +
        "  x.method(123);" +
        "};,
        "Function MyClass3.prototype.method: called with 1 argument(s). " +
        "Argument type mismatch: expected: string actual: number");
  }
