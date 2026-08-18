// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1024_AdditionalCase1() throws Exception {
     testTypes(
        "/** @constructor */\n" +
        "function Foo() {}\n" +
        "Foo.prototype = {bar: 1};\n");
  }