// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineMultipleValuesWithBackslashes() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("test", "\\\\\\\\192.168.1.91\\\\test");
        props.addProperty("test", "\\\\\\\\192.168.1.92\\\\test2");
        ExtendedProperties props2 = new ExtendedProperties();
        props2.combine(props);
        Vector values = props2.getVector("test");
        assertEquals(2, values.size());
        assertEquals("\\\\\\\\192.168.1.91\\\\test", values.elementAt(0));
        assertEquals("\\\\\\\\192.168.1.92\\\\test2", values.elementAt(1));
    }
