// com/google/javascript/jscomp/NodeUtilTest.java::testLocalValue_functionAndRegexp
public void testLocalValue_functionAndRegexp() throws Exception {
    assertTrue(testLocalValue("(function(){})"));
    assertTrue(testLocalValue("/a/"));
  }