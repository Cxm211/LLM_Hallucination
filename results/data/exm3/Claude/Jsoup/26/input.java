// buggy function
    public Document clean(Document dirtyDocument) {
        Validate.notNull(dirtyDocument);

        Document clean = Document.createShell(dirtyDocument.baseUri());
            copySafeNodes(dirtyDocument.body(), clean.body());

        return clean;
    }

// trigger testcase
// org/jsoup/safety/CleanerTest.java::handlesFramesets
@Test public void handlesFramesets() {
        String dirty = "<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>";
        String clean = Jsoup.clean(dirty, Whitelist.basic());
        assertEquals("", clean); // nothing good can come out of that

        Document dirtyDoc = Jsoup.parse(dirty);
        Document cleanDoc = new Cleaner(Whitelist.basic()).clean(dirtyDoc);
        assertFalse(cleanDoc == null);
        assertEquals(0, cleanDoc.body().childNodes().size());
    }
