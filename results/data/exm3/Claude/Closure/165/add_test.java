// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue725_conflictingPropertyTypes() throws Exception {
  testTypes(
      "/** @typedef {{name: string}} */ var RecordType1;" +
      "/** @typedef {{name: number}} */ var RecordType2;" +
      "/** @param {RecordType1} rec */ function f(rec) {" +
      "  var x = rec.name - 5;" +
      "}",
      "operator - cannot be applied to (string,number)");
}