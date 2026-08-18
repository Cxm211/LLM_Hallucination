// com/google/javascript/jscomp/InlineFunctionsTest.java
public void testIssue423_AdditionalCase1() {
    test(
        "(function($) {\n" +
        "  function helper() {\n" +
        "    return 42;\n" +
        "  }\n" +
        "  $.fn.test = function() {\n" +
        "    var result = obj.method.call(this);\n" +
        "    return helper();\n" +
        "  };\n" +
        "})(jQuery)",
        "(function($){" +
        "  $.fn.test=function(){" +
        "    var result=obj.method.call(this);" +
        "    return 42" +
        "  }" +
        "})(jQuery)");
  }