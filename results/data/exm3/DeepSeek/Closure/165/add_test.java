// com/google/javascript/jscomp/TypeCheckTest.java
public void testRecordMissingProperty2() throws Exception {
    testTypes(
        "/** @typedef {{x: number}} */ var RecA;" +
        "/** @typedef {{y: string}} */ var RecB;" +
        "/** @param {RecA} a */ function g(a) {" +
        "  alert(a.y);" +
        "}",
        "Property y never defined on a");
  }
