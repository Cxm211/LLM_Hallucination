// com/google/javascript/jscomp/ConvertToDottedPropertiesTest.java
public void testControlCharactersInMiddle() {
  testSame("a['a\u0000b']");
  testSame("a['a\u0001b']");
  testSame("a['a\u001fb']");
}