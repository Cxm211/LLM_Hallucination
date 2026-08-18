// buggy function
    public String getValue() {
        return val;
    }

// trigger testcase
// org/jsoup/nodes/AttributeTest.java::booleanAttributesAreEmptyStringValues
@Test public void booleanAttributesAreEmptyStringValues() {
        Document doc = Jsoup.parse("<div hidden>");
        Attributes attributes = doc.body().child(0).attributes();
        assertEquals("", attributes.get("hidden"));

        Attribute first = attributes.iterator().next();
        assertEquals("hidden", first.getKey());
        assertEquals("", first.getValue());
    }
