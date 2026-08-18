// buggy function
    private static final String[] TagSearchSpecial = sort(new String[]{"address", "applet", "area", "article", "aside", "base", "basefont", "bgsound",
        "blockquote", "body", "br", "button", "caption", "center", "col", "colgroup", "command", "dd",
        "details", "dir", "div", "dl", "dt", "embed", "fieldset", "figcaption", "figure", "footer", "form",
        "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html",
        "iframe", "img", "input", "isindex", "li", "link", "listing", "marquee", "menu", "meta", "nav",
        "noembed", "noframes", "noscript", "object", "ol", "p", "param", "plaintext", "pre", "script",
        "section", "select", "style", "summary", "table", "tbody", "td", "textarea", "tfoot", "th", "thead",
        "title", "tr", "ul", "wbr", "xmp"});

    private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        int depth = stack.size() -1;
        for (int pos = depth; pos >= 0; pos--) {
            Element el = stack.get(pos);
            String elName = el.nodeName();
            if (inSorted(elName, targetNames))
                return true;
            if (inSorted(elName, baseTypes))
                return false;
            if (extraTypes != null && inSorted(elName, extraTypes))
                return false;
        }
        Validate.fail("Should not be reachable");
        return false;
    }

// trigger testcase
// org/jsoup/parser/HtmlParserTest.java::handlesDeepStack
@Test public void handlesDeepStack() {
        // inspired by http://sv.stargate.wikia.com/wiki/M2J and https://github.com/jhy/jsoup/issues/955
        // I didn't put it in the integration tests, because explorer and intellij kept dieing trying to preview/index it

        // Arrange
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<dl><dd>");
        }
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("</dd></dl>");
        }

        // Act
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");

        // Assert
        assertEquals(2, doc.body().childNodeSize());
        assertEquals(25000, doc.select("dd").size());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }
