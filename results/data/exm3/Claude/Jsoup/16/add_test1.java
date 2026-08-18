// org/jsoup/nodes/DocumentTypeTest.java
@Test(expected = IllegalArgumentException.class)
public void constructorValidationThrowsExceptionOnWhitespaceName() {
    DocumentType fail = new DocumentType("   \t\n", "", "", "");
}