// com/google/javascript/jscomp/TypeCheckTest.java
public void testInferPropertyUnionMissing() throws Exception {
    testTypes(
        "/** @return {{a: (number|undefined), b: (number|undefined)}} */" +
         "function f(x) {" +
         "  if (x == 1) {" +
         "    return {a: 1};" +
         "  } else if (x == 2) {" +
         "    return {b: 2};" +
         "  } else {" +
         "    return {};" +
         "  }" +
         "});
  }
