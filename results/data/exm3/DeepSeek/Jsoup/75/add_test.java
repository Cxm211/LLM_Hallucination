// org/jsoup/nodes/ElementTest.java
@Test
    public void nonBooleanNullAttribute() {
        Element div = new Element("div");
        div.attributes().put("custom", null);
        assertEquals("<div custom=\"\">", div.outerHtml());
    }
