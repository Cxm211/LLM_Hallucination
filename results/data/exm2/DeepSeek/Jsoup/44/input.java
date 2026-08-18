    protected boolean processStartTag(String name) {
        return process(start.reset().name(name));
    }

    public boolean processStartTag(String name, Attributes attrs) {
        start.reset();
        start.nameAttr(name, attrs);
        return process(start);
    }

    protected boolean processEndTag(String name) {
        return process(end.reset().name(name));
    }

// trigger testcase
@Test
    public void testInvalidTableContents() throws IOException {
        File in = ParseTest.getFile("/htmltests/table-invalid-elements.html");
        Document doc = Jsoup.parse(in, "UTF-8");
        doc.outputSettings().prettyPrint(true);
        String rendered = doc.toString();
        int endOfEmail = rendered.indexOf("Comment");
        int guarantee = rendered.indexOf("Why am I here?");
        assertTrue("Comment not found", endOfEmail > -1);
        assertTrue("Search text not found", guarantee > -1);
        assertTrue("Search text did not come after comment", guarantee > endOfEmail);
    }
