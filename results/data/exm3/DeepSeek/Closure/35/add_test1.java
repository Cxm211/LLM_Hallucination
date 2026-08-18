// com/google/javascript/jscomp/TypeCheckTest.java
public void testInferPropertyUnionWithDeclared() throws Exception {
    testTypes(
        "/** @return {{prop: (string|undefined)}} */" +
         "function f(x) {" +
         "  if (x) {" +
         "    var obj = {};" +
         "    /** @type {string} */" +
         "    obj.prop = 'hello';" +
         "    return obj;" +
         "  } else {" +
         "    return {};" +
         "  }" +
         "});
  }
