// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue1024_AdditionalCase3() throws Exception {
     testTypes(
        "/** @interface */\n" +
        "function IFoo() {}\n" +
        "IFoo.prototype = {method: function() {}};\n");
  }