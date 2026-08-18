// com/google/javascript/jscomp/TypeCheckTest.java
public void testPrototypeAssignmentInferredOwner() throws Exception {
    testTypes(
        "var Foo = function() {};" +
        "Foo.prototype = { method: function() {} };" +
        "(new Foo()).method();",
        "");
  }
