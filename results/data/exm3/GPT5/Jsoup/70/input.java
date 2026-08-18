// buggy function
    static boolean preserveWhitespace(Node node) {
        // looks only at this element and five levels up, to prevent recursion & needless stack searches
        if (node != null && node instanceof Element) {
            Element el = (Element) node;
                if (el.tag.preserveWhitespace())
                    return true;
                else
                    return el.parent() != null && el.parent().tag.preserveWhitespace();
        }
        return false;
    }

// trigger testcase
// org/jsoup/nodes/ElementTest.java::testKeepsPreTextAtDepth
@Test public void testKeepsPreTextAtDepth() {
        String h = "<pre><code><span><b>code\n\ncode</b></span></code></pre>";
        Document doc = Jsoup.parse(h);
        assertEquals("code\n\ncode", doc.text());
        assertEquals("<pre><code><span><b>code\n\ncode</b></span></code></pre>", doc.body().html());
    }
