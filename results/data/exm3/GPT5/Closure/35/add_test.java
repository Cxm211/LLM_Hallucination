// com/google/javascript/jscomp/TypeCheckTest.java::testIssue669Variant
public void testIssue669Variant() throws Exception {
    testTypes(
        "/** @return {{prop1: (number|undefined)}} */" +
         "function f(a) {" +
         "  var results;" +
         "  if (a) {" +
         "    results = {prop1: 5};" +
         "  } else {" +
         "    results = {prop2: 3};" +
         "  }" +
         "  return results;" +
         "}");
  }