// buggy function
  public final boolean isEmptyType() {
    return isNoType() || isNoObjectType() || isNoResolvedType();
  }

// trigger testcase
// com/google/javascript/jscomp/TypeCheckTest.java::testIssue301
public void testIssue301() throws Exception {
    testTypes(
        "Array.indexOf = function() {};" +
        "var s = 'hello';" +
        "alert(s.toLowerCase.indexOf('1'));",
        "Property indexOf never defined on String.prototype.toLowerCase");
  }

// com/google/javascript/rhino/jstype/FunctionTypeTest.java::testEmptyFunctionTypes
public void testEmptyFunctionTypes() {
    assertTrue(LEAST_FUNCTION_TYPE.isEmptyType());
    assertFalse(GREATEST_FUNCTION_TYPE.isEmptyType());
  }
