// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue725_multiplePropertiesOneDifferent() throws Exception {
  testTypes(
      "/** @typedef {{name: string, age: number}} */ var RecordType1;" +
      "/** @typedef {{name: string, city: string}} */ var RecordType2;" +
      "/** @param {RecordType1} rec */ function f(rec) {" +
      "  alert(rec.city);" +
      "}",
      "Property city never defined on rec");
}