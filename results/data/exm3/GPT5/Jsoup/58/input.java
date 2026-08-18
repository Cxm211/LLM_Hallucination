// buggy function
    public static boolean isValid(String bodyHtml, Whitelist whitelist) {
        return new Cleaner(whitelist).isValid(parseBodyFragment(bodyHtml, ""));
    }

    public static List<Node> parseFragment(String fragmentHtml, Element context, String baseUri) {
        HtmlTreeBuilder treeBuilder = new HtmlTreeBuilder();
        return treeBuilder.parseFragment(fragmentHtml, context, baseUri, ParseErrorList.noTracking(), treeBuilder.defaultSettings());
    }

    public boolean isValid(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
        int numDiscarded = copySafeNodes(dirtyDocument.body(), clean.body());
        return numDiscarded == 0;
    }

// trigger testcase
// org/jsoup/safety/CleanerTest.java::testIsValidBodyHtml
@Test public void testIsValidBodyHtml() {
        String ok = "<p>Test <b><a href='http://example.com/' rel='nofollow'>OK</a></b></p>";
        String ok1 = "<p>Test <b><a href='http://example.com/'>OK</a></b></p>"; // missing enforced is OK because still needs run thru cleaner
        String nok1 = "<p><script></script>Not <b>OK</b></p>";
        String nok2 = "<p align=right>Test Not <b>OK</b></p>";
        String nok3 = "<!-- comment --><p>Not OK</p>"; // comments and the like will be cleaned
        String nok4 = "<html><head>Foo</head><body><b>OK</b></body></html>"; // not body html
        String nok5 = "<p>Test <b><a href='http://example.com/' rel='nofollowme'>OK</a></b></p>";
        String nok6 = "<p>Test <b><a href='http://example.com/'>OK</b></p>"; // missing close tag
        String nok7 = "</div>What";
        assertTrue(Jsoup.isValid(ok, Whitelist.basic()));
        assertTrue(Jsoup.isValid(ok1, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok1, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok2, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok3, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok4, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok5, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok6, Whitelist.basic()));
        assertFalse(Jsoup.isValid(ok, Whitelist.none()));
        assertFalse(Jsoup.isValid(nok7, Whitelist.basic()));
    }

// org/jsoup/safety/CleanerTest.java::testIsValidDocument
@Test public void testIsValidDocument() {
        String ok = "<html><head></head><body><p>Hello</p></body><html>";
        String nok = "<html><head><script>woops</script><title>Hello</title></head><body><p>Hello</p></body><html>";

        Whitelist relaxed = Whitelist.relaxed();
        Cleaner cleaner = new Cleaner(relaxed);
        Document okDoc = Jsoup.parse(ok);
        assertTrue(cleaner.isValid(okDoc));
        assertFalse(cleaner.isValid(Jsoup.parse(nok)));
        assertFalse(new Cleaner(Whitelist.none()).isValid(okDoc));
    }
