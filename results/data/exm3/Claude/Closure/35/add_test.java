// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue669_AdditionalCase1() throws Exception {
    testTypes(
        "/** @return {{prop1: number, prop2: string}} */" +
         "function f(a) {" +
         "  var results;" +
         "  if (a) {" +
         "    results = {};" +
         "    results.prop1 = 42;" +
         "    results.prop2 = 'hello';" +
         "  } else {" +
         "    results = {prop1: 99, prop3: true};" +
         "  }" +
         "  return results;" +
         "}");
  }