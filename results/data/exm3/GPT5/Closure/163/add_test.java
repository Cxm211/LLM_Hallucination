// com/google/javascript/jscomp/CrossModuleMethodMotionTest.java::testIssue600
public void testIssue600_additionalQuotedKey() {
    testSame(
        createModuleChain(
            "var jQuery1 = (function() {\n" +
            "  var jQuery2 = function() {};\n" +
            "  var theLoneliestNumber = 1;\n" +
            "  jQuery2.prototype = {\n" +
            "    'size': function() {\n" +
            "      return theLoneliestNumber;\n" +
            "    }\n" +
            "  };\n" +
            "  return jQuery2;\n" +
            "})();\n",

            "(function() {" +
            "  var div = jQuery1('div');" +
            "  div.size();" +
            "})();"));
  }