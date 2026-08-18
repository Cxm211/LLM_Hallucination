// org/apache/commons/jxpath/ri/model/beans/BadlyImplementedFactoryTest.java
public void testBadFactoryImplementationAttributeWithValue() {
        try {
            context.createPath("foo/@bar", "value");
            fail("should fail with JXPathException caused by JXPathAbstractFactoryException");
        } catch (JXPathException e) {
            assertTrue(e.getCause() instanceof JXPathAbstractFactoryException);
        }
    }
