// org/jsoup/nodes/DocumentTypeTest.java
@Test(expected = IllegalArgumentException.class)
public void constructorRejectsNullName() {
    DocumentType fail = new DocumentType(null, "", "", "");
}