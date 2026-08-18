// com/google/javascript/jscomp/TypeCheckTest.java
public void testEmptyRecordMissingProperty() throws Exception {
    testTypes(
        "/** @typedef {{}} */ var EmptyRec;" +
        "/** @param {EmptyRec} r */ function h(r) {" +
        "  alert(r.foo);" +
        "}",
        "Property foo never defined on r");
  }
