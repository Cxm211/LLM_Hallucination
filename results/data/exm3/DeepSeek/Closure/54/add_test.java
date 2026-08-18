// com/google/javascript/jscomp/TypeCheckTest.java
public void testPrototypeAssignmentInheritance() throws Exception {
    testTypes(
        "/** @constructor */ function Foo() {}" +
        "Foo.prototype.method = function() {};" +
        "/** @constructor */ function Bar() {}" +
        "Bar.prototype = new Foo();" +
        "var x = new Bar();" +
        "x.method();",
        "");
  }
