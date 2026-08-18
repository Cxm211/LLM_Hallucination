// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue669_AdditionalCase2() throws Exception {
    testTypes(
        "/** @return {{prop1: (string|undefined), prop2: (number|undefined)}} */" +
         "function f(a, b) {" +
         "  var results;" +
         "  if (a) {" +
         "    results = {prop1: 'test'};" +
         "  } else if (b) {" +
         "    results = {prop2: 123};" +
         "  } else {" +
         "    results = {};" +
         "  }" +
         "  return results;" +
         "}");
  }