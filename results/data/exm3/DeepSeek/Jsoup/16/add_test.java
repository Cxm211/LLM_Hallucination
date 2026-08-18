// org/jsoup/nodes/DocumentTypeTest.java
@Test(expected = IllegalArgumentException.class)
    public void constructorValidationThrowsExceptionOnNullName() {
        new DocumentType(null, "", "", "");
    }
