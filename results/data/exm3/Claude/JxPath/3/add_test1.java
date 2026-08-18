// org/apache/commons/jxpath/ri/model/beans/BadlyImplementedFactoryTest.java
public void testCreatePathWithValue() {
    try {
        context.createPathAndSetValue("foo/bar", "testValue");
        fail("should fail with JXPathException caused by JXPathAbstractFactoryException");
    } catch (JXPathException e) {
        assertTrue(e.getCause() instanceof JXPathAbstractFactoryException);
    }
}