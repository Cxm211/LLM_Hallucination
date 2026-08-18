// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineNested() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("x.y.z", "xyz");
        props.setProperty("x.y.w", "xyw");
        props.setProperty("x.a", "xa");
        ExtendedProperties target = new ExtendedProperties();
        target.combine(props);
        assertEquals("xyz", target.getProperty("x.y.z"));
        assertEquals("xyw", target.getProperty("x.y.w"));
        assertEquals("xa", target.getProperty("x.a"));
        ExtendedProperties subsetX = target.subset("x");
        assertNotNull(subsetX);
        assertEquals("xyz", subsetX.getProperty("y.z"));
        assertEquals("xyw", subsetX.getProperty("y.w"));
        assertEquals("xa", subsetX.getProperty("a"));
        ExtendedProperties subsetXY = target.subset("x.y");
        assertNotNull(subsetXY);
        assertEquals("xyz", subsetXY.getProperty("z"));
        assertEquals("xyw", subsetXY.getProperty("w"));
    }
