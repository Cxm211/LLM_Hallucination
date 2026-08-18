// buggy code
     Iterates the input and copies trusted nodes (tags, attributes, text) into the destination.
     */
    private int copySafeNodes(Element root, Element destination) {
        List<Node> sourceChildren = root.childNodes();
        int numDiscarded = 0;

        for (Node source : sourceChildren) {
            if (source instanceof Element) {
                Element sourceEl = (Element) source;

                if (whitelist.isSafeTag(sourceEl.tagName())) { // safe, clone and copy safe attrs
                    ElementMeta meta = createSafeElement(sourceEl);
                    Element destChild = meta.el;
                    destination.appendChild(destChild);

                    numDiscarded += meta.numAttribsDiscarded;
                    numDiscarded += copySafeNodes(sourceEl, destChild);
                } else {
                    numDiscarded++;
                    numDiscarded += copySafeNodes(sourceEl, destination);
                }
            } else if (source instanceof TextNode) {
                TextNode sourceText = (TextNode) source;
                TextNode destText = new TextNode(sourceText.getWholeText(), source.baseUri());
                destination.appendChild(destText);
            }
        }
        return numDiscarded;


    }

// relevant test
// org.jsoup.safety.CleanerTest::simpleBehaviourTest
    @Test public void simpleBehaviourTest() {
        String h = "<div><p class=foo><a href='http://evil.com'>Hello <b id=bar>there</b>!</a></div>";
        String cleanHtml = Jsoup.clean(h, Whitelist.simpleText());

        assertEquals("Hello <b>there</b>!", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::simpleBehaviourTest2
    @Test public void simpleBehaviourTest2() {
        String h = "Hello <b>there</b>!";
        String cleanHtml = Jsoup.clean(h, Whitelist.simpleText());

        assertEquals("Hello <b>there</b>!", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::basicBehaviourTest
    @Test public void basicBehaviourTest() {
        String h = "<div><p><a href='javascript:sendAllMoney()'>Dodgy</a> <A HREF='HTTP://nice.com'>Nice</a></p><blockquote>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic());

        assertEquals("<p><a rel=\"nofollow\">Dodgy</a> <a href=\"http://nice.com\" rel=\"nofollow\">Nice</a></p><blockquote>Hello</blockquote>",
                TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::basicWithImagesTest
    @Test public void basicWithImagesTest() {
        String h = "<div><p><img src='http://example.com/' alt=Image></p><p><img src='ftp://ftp.example.com'></p></div>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basicWithImages());
        assertEquals("<p><img src=\"http://example.com/\" alt=\"Image\" /></p><p><img /></p>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRelaxed
    @Test public void testRelaxed() {
        String h = "<h1>Head</h1><table><tr><td>One<td>Two</td></tr></table>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<h1>Head</h1><table><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testDropComments
    @Test public void testDropComments() {
        String h = "<p>Hello<!-- no --></p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<p>Hello</p>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropXmlProc
    @Test public void testDropXmlProc() {
        String h = "<?import namespace=\"xss\"><p>Hello</p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<p>Hello</p>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropScript
    @Test public void testDropScript() {
        String h = "<SCRIPT SRC=//ha.ckers.org/.j><SCRIPT>alert(/XSS/.source)</SCRIPT>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropImageScript
    @Test public void testDropImageScript() {
        String h = "<IMG SRC=\"javascript:alert('XSS')\">";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<img />", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testCleanJavascriptHref
    @Test public void testCleanJavascriptHref() {
        String h = "<A HREF=\"javascript:document.location='http://www.google.com/'\">XSS</A>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<a>XSS</a>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testDropsUnknownTags
    @Test public void testDropsUnknownTags() {
        String h = "<p><custom foo=true>Test</custom></p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<p>Test</p>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testHandlesEmptyAttributes
    @Test public void testHandlesEmptyAttributes() {
        String h = "<img alt=\"\" src= unknown=''>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basicWithImages());
        assertEquals("<img alt=\"\" />", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testIsValid
    @Test public void testIsValid() {
        String ok = "<p>Test <b><a href='http://example.com/'>OK</a></b></p>";
        String nok1 = "<p><script></script>Not <b>OK</b></p>";
        String nok2 = "<p align=right>Test Not <b>OK</b></p>";
        String nok3 = "<!-- comment --><p>Not OK</p>"; 
        assertTrue(Jsoup.isValid(ok, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok1, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok2, Whitelist.basic()));
        assertFalse(Jsoup.isValid(nok3, Whitelist.basic()));
    }

// org.jsoup.safety.CleanerTest::resolvesRelativeLinks
    @Test public void resolvesRelativeLinks() {
        String html = "<a href='/foo'>Link</a><img src='/bar'>";
        String clean = Jsoup.clean(html, "http://example.com/", Whitelist.basicWithImages());
        assertEquals("<a href=\"http://example.com/foo\" rel=\"nofollow\">Link</a>\n<img src=\"http://example.com/bar\" />", clean);
    }

// org.jsoup.safety.CleanerTest::preservesRelativeLinksIfConfigured
    @Test public void preservesRelativeLinksIfConfigured() {
        String html = "<a href='/foo'>Link</a><img src='/bar'> <img src='javascript:alert()'>";
        String clean = Jsoup.clean(html, "http://example.com/", Whitelist.basicWithImages().preserveRelativeLinks(true));
        assertEquals("<a href=\"/foo\" rel=\"nofollow\">Link</a>\n<img src=\"/bar\" /> \n<img />", clean);
    }

// org.jsoup.safety.CleanerTest::dropsUnresolvableRelativeLinks
    @Test public void dropsUnresolvableRelativeLinks() {
        String html = "<a href='/foo'>Link</a>";
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<a rel=\"nofollow\">Link</a>", clean);
    }

// org.jsoup.safety.CleanerTest::handlesCustomProtocols
    @Test public void handlesCustomProtocols() {
        String html = "<img src='cid:12345' /> <img src='data:gzzt' />";
        String dropped = Jsoup.clean(html, Whitelist.basicWithImages());
        assertEquals("<img /> \n<img />", dropped);

        String preserved = Jsoup.clean(html, Whitelist.basicWithImages().addProtocols("img", "src", "cid", "data"));
        assertEquals("<img src=\"cid:12345\" /> \n<img src=\"data:gzzt\" />", preserved);
    }

// org.jsoup.safety.CleanerTest::handlesAllPseudoTag
    @Test public void handlesAllPseudoTag() {
        String html = "<p class='foo' src='bar'><a class='qux'>link</a></p>";
        Whitelist whitelist = new Whitelist()
                .addAttributes(":all", "class")
                .addAttributes("p", "style")
                .addTags("p", "a");

        String clean = Jsoup.clean(html, whitelist);
        assertEquals("<p class=\"foo\"><a class=\"qux\">link</a></p>", clean);
    }

// org.jsoup.safety.CleanerTest::addsTagOnAttributesIfNotSet
    @Test public void addsTagOnAttributesIfNotSet() {
        String html = "<p class='foo' src='bar'>One</p>";
        Whitelist whitelist = new Whitelist()
            .addAttributes("p", "class");
        
        String clean = Jsoup.clean(html, whitelist);
        assertEquals("<p class=\"foo\">One</p>", clean);
    }

// org.jsoup.safety.CleanerTest::supplyOutputSettings
    @Test public void supplyOutputSettings() {
        
        Document.OutputSettings os = new Document.OutputSettings();
        os.prettyPrint(false);
        os.escapeMode(Entities.EscapeMode.extended);

        String html = "<div><p>&bernou;</p></div>";
        String customOut = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed(), os);
        String defaultOut = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed());
        assertNotSame(defaultOut, customOut);

        assertEquals("<div><p>&bernou;</p></div>", customOut);
        assertEquals("<div>\n" +
            " <p>ℬ</p>\n" +
            "</div>", defaultOut);

        os.charset("ASCII");
        os.escapeMode(Entities.EscapeMode.base);
        String customOut2 = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed(), os);
        assertEquals("<div><p>&#8492;</p></div>", customOut2);
    }

// org.jsoup.safety.CleanerTest::handlesFramesets
    @Test public void handlesFramesets() {
        String dirty = "<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>";
        String clean = Jsoup.clean(dirty, Whitelist.basic());
        assertEquals("", clean); 

        Document dirtyDoc = Jsoup.parse(dirty);
        Document cleanDoc = new Cleaner(Whitelist.basic()).clean(dirtyDoc);
        assertFalse(cleanDoc == null);
        assertEquals(0, cleanDoc.body().childNodes().size());
    }

// org.jsoup.safety.CleanerTest::cleansInternationalText
    @Test public void cleansInternationalText() {
        assertEquals("привет", Jsoup.clean("привет", Whitelist.none()));
    }
