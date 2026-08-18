// com/google/javascript/jscomp/ConvertToDottedPropertiesTest.java
public void testDoNotConvertNonASCIIIdentifiers() {
  testSame("a['\u00e9']");
  testSame("a['\u00f1']");
  testSame("a['\u0100']");
  testSame("a['a\u0080']");
  testSame("a['\u007fb']");
}