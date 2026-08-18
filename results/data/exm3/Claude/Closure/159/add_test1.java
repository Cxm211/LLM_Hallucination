// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testIssue423_AdditionalCase2() {
    test(
        "(function() {\n" +
        "  function foo() {\n" +
        "    bar.baz.call(this);\n" +
        "  }\n" +
        "  foo();\n" +
        "})()",
        "(function(){" +
        "  {" +
        "    bar.baz.call(this)" +
        "  }" +
        "})()");
  }