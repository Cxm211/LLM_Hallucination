// com/google/javascript/jscomp/ConvertToDottedPropertiesTest.java
public void testBoundaryASCIICharacters() {
  testSame("a['\u007f']");
  test("a['z']", "a.z");
  test("a['Z']", "a.Z");
  testSame("a['{']');
}