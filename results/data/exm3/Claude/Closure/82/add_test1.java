// com/google/javascript/jscomp/TypeCheckTest.java
public void testIssue301WithChainedCalls() throws Exception {
  testTypes(
      "Array.indexOf = function() {};" +
      "var s = 'hello';" +
      "var result = s.toLowerCase().toUpperCase();" +
      "alert(result.indexOf('1'));",
      "Property indexOf never defined on String");
}