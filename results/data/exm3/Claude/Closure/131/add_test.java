// com/google/javascript/jscomp/ConvertToDottedPropertiesTest.java
public void testConvertValidASCIIIdentifiers() {
  test("a['b']", "a.b");
  test("a['$']", "a.$");
  test("a['_']", "a._");
  test("a['abc123']", "a.abc123");
  test("a['$_abc']", "a.$_abc");
}