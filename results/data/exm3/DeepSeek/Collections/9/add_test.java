// org/apache/commons/collections/TestExtendedProperties.java
public void testCombineMultipleValues() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value1");
        props.addProperty("key", "value2");
        ExtendedProperties props2 = new ExtendedProperties();
        props2.combine(props);
        Vector values = props2.getVector("key");
        assertEquals(2, values.size());
        assertEquals("value1", values.elementAt(0));
        assertEquals("value2", values.elementAt(1));
    }
