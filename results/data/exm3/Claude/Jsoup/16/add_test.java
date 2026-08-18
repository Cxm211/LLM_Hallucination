// org/jsoup/nodes/DocumentTypeTest.java
@Test(expected = IllegalArgumentException.class)
public void constructorValidationThrowsExceptionOnNullName() {
    DocumentType fail = new DocumentType(null, "", "", "");
}