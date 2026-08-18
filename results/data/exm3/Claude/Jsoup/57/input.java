// buggy function
    public void removeIgnoreCase(String key) {
        Validate.notEmpty(key);
        if (attributes == null)
            return;
        for (Iterator<String> it = attributes.keySet().iterator(); it.hasNext(); ) {
            String attrKey = it.next();
            if (attrKey.equalsIgnoreCase(key))
                attributes.remove(attrKey);
        }
    }

// trigger testcase
// org/jsoup/nodes/ElementTest.java::testChainedRemoveAttributes
@Test
    public void testChainedRemoveAttributes() {
        String html = "<a one two three four>Text</a>";
        Document doc = Jsoup.parse(html);
        Element a = doc.select("a").first();
        a
            .removeAttr("zero")
            .removeAttr("one")
            .removeAttr("two")
            .removeAttr("three")
            .removeAttr("four")
            .removeAttr("five");
        assertEquals("<a>Text</a>", a.outerHtml());
    }
