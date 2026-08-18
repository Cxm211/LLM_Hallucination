// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineOverwrite() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("a.b", "val1");
        ExtendedProperties props2 = new ExtendedProperties();
        props2.setProperty("a.b", "val2");
        props.combine(props2);
        assertEquals("val2", props.getProperty("a.b"));
        ExtendedProperties subset = props.subset("a");
        assertNotNull(subset);
        assertEquals("val2", subset.getProperty("b"));
    }
