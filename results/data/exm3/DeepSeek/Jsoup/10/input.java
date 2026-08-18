// buggy function
    public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        String relUrl = attr(attributeKey);
        if (!hasAttr(attributeKey)) {
            return ""; // nothing to make absolute with
        } else {
            URL base;
            try {
                try {
                    base = new URL(baseUri);
                } catch (MalformedURLException e) {
                    // the base is unsuitable, but the attribute may be abs on its own, so try that
                    URL abs = new URL(relUrl);
                    return abs.toExternalForm();
                }
                // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
                URL abs = new URL(base, relUrl);
                return abs.toExternalForm();
            } catch (MalformedURLException e) {
                return "";
            }
        }
    }

// trigger testcase
// org/jsoup/nodes/NodeTest.java::absHandlesRelativeQuery
@Test public void absHandlesRelativeQuery() {
        Document doc = Jsoup.parse("<a href='?foo'>One</a> <a href='bar.html?foo'>Two</a>", "http://jsoup.org/path/file?bar");

        Element a1 = doc.select("a").first();
        assertEquals("http://jsoup.org/path/file?foo", a1.absUrl("href"));

        Element a2 = doc.select("a").get(1);
        assertEquals("http://jsoup.org/path/bar.html?foo", a2.absUrl("href"));
    }
