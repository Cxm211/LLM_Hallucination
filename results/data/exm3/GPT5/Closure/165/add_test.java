// com/google/javascript/jscomp/TypeCheckTest.java::testIssue725
public void testIssue725() throws Exception {
    testTypes(
        "/** @typedef {{name: string}} */ var RecordType1;" +
        "/** @typedef {{name2: string}} */ var RecordType2;" +
        "/** @param {RecordType1} rec */ function f(rec) {" +
        "  alert(rec.name2);" +
        "}",
        "Property name2 never defined on rec");

    // Additional check: symmetric case
    testTypes(
        "/** @typedef {{name: string}} */ var RecordType1;" +
        "/** @typedef {{name2: string}} */ var RecordType2;" +
        "/** @param {RecordType2} rec */ function g(rec) {" +
        "  alert(rec.name);" +
        "}",
        "Property name never defined on rec");
  }