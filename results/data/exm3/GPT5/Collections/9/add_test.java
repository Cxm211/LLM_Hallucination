// org/apache/commons/collections/TestExtendedProperties.java::testCombineProcessedWithoutPreRead
public void testCombineProcessedWithoutPreRead() {
        ExtendedProperties src = new ExtendedProperties();
        src.setProperty("path", "\\\\\\\\host\\\\share");

        ExtendedProperties dest = new ExtendedProperties();
        dest.combine(src);

        assertEquals("\\\\host\\share", dest.getProperty("path"));
    }