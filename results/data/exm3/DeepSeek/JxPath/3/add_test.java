// org/apache/commons/jxpath/ri/model/beans/BadlyImplementedFactoryTest.java
public void testBadFactoryImplementationAttribute() {
        try {
            context.createPath("foo/@bar");
            fail("should fail with JXPathException caused by JXPathAbstractFactoryException");
        } catch (JXPathException e) {
            assertTrue(e.getCause() instanceof JXPathAbstractFactoryException);
        }
    }
