// org/apache/commons/jxpath/ri/model/beans/BadlyImplementedFactoryTest.java
public void testCreatePathWithEscapableCharacters() {
    try {
        context.createPath("foo[@name='test']");
        fail("should fail with JXPathException caused by JXPathAbstractFactoryException");
    } catch (JXPathException e) {
        assertTrue(e.getCause() instanceof JXPathAbstractFactoryException);
    }
}