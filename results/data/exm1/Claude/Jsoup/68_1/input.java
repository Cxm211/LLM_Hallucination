// buggy code
    private boolean inSpecificScope(String[] targetNames, String[] baseTypes, String[] extraTypes) {
        // https://html.spec.whatwg.org/multipage/parsing.html#has-an-element-in-the-specific-scope
        int bottom = stack.size() -1;
        if (bottom > MaxScopeSearchDepth) {
            bottom = MaxScopeSearchDepth;
        }
        final int top = bottom > MaxScopeSearchDepth ? bottom - MaxScopeSearchDepth : 0;
        // don't walk too far up the tree

        for (int pos = bottom; pos >= top; pos--) {
            final String elName = stack.get(pos).nodeName();
            if (inSorted(elName, targetNames))
                return true;
            if (inSorted(elName, baseTypes))
                return false;
            if (extraTypes != null && inSorted(elName, extraTypes))
                return false;
        }
        //Validate.fail("Should not be reachable"); // would end up false because hitting 'html' at root (basetypes)
        return false;
    }

// relevant test
// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyStyle
    @Test public void handlesKnownEmptyStyle() {
        String h = "<html><head><style /><meta name=foo></head><body>One</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><style></style><meta name=\"foo\"></head><body>One</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyTitle
    @Test public void handlesKnownEmptyTitle() {
        String h = "<html><head><title /><meta name=foo></head><body>One</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><title></title><meta name=\"foo\"></head><body>One</body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesKnownEmptyIframe
    @Test public void handlesKnownEmptyIframe() {
        String h = "<p>One</p><iframe id=1 /><p>Two";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body><p>One</p><iframe id=\"1\"></iframe><p>Two</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesSolidusAtAttributeEnd
    @Test public void handlesSolidusAtAttributeEnd() {
        
        String h = "<a href=/>link</a>";
        Document doc = Jsoup.parse(h);
        assertEquals("<a href=\"/\">link</a>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesMultiClosingBody
    @Test public void handlesMultiClosingBody() {
        String h = "<body><p>Hello</body><p>there</p></body></body></html><p>now";
        Document doc = Jsoup.parse(h);
        assertEquals(3, doc.select("p").size());
        assertEquals(3, doc.body().children().size());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedDefinitionLists
    @Test public void handlesUnclosedDefinitionLists() {
        
        String h = "<dt>Foo<dd>Bar<dt>Qux<dd>Zug";
        Document doc = Jsoup.parse(h);
        assertEquals(0, doc.select("dl").size()); 
        assertEquals(4, doc.select("dt, dd").size());
        Elements dts = doc.select("dt");
        assertEquals(2, dts.size());
        assertEquals("Zug", dts.get(1).nextElementSibling().text());
    }

// org.jsoup.parser.HtmlParserTest::handlesBlocksInDefinitions
    @Test public void handlesBlocksInDefinitions() {
        
        String h = "<dl><dt><div id=1>Term</div></dt><dd><div id=2>Def</div></dd></dl>";
        Document doc = Jsoup.parse(h);
        assertEquals("dt", doc.select("#1").first().parent().tagName());
        assertEquals("dd", doc.select("#2").first().parent().tagName());
        assertEquals("<dl><dt><div id=\"1\">Term</div></dt><dd><div id=\"2\">Def</div></dd></dl>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesFrames
    @Test public void handlesFrames() {
        String h = "<html><head><script></script><noscript></noscript></head><frameset><frame src=foo></frame><frame src=foo></frameset></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\"><frame src=\"foo\"></frameset></html>",
                TextUtil.stripNewlines(doc.html()));
        
    }

// org.jsoup.parser.HtmlParserTest::ignoresContentAfterFrameset
    @Test public void ignoresContentAfterFrameset() {
        String h = "<html><head><title>One</title></head><frameset><frame /><frame /></frameset><table></table></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><title>One</title></head><frameset><frame><frame></frameset></html>", TextUtil.stripNewlines(doc.html()));
        
    }

// org.jsoup.parser.HtmlParserTest::handlesJavadocFont
    @Test public void handlesJavadocFont() {
        String h = "<TD BGCOLOR=\"#EEEEFF\" CLASS=\"NavBarCell1\">    <A HREF=\"deprecated-list.html\"><FONT CLASS=\"NavBarFont1\"><B>Deprecated</B></FONT></A>&nbsp;</TD>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("Deprecated", a.text());
        assertEquals("font", a.child(0).tagName());
        assertEquals("b", a.child(0).child(0).tagName());
    }

// org.jsoup.parser.HtmlParserTest::handlesBaseWithoutHref
    @Test public void handlesBaseWithoutHref() {
        String h = "<head><base target='_blank'></head><body><a href=/foo>Test</a></body>";
        Document doc = Jsoup.parse(h, "http://example.com/");
        Element a = doc.select("a").first();
        assertEquals("/foo", a.attr("href"));
        assertEquals("http://example.com/foo", a.attr("abs:href"));
    }

// org.jsoup.parser.HtmlParserTest::normalisesDocument
    @Test public void normalisesDocument() {
        String h = "<!doctype html>One<html>Two<head>Three<link></head>Four<body>Five </body>Six </html>Seven ";
        Document doc = Jsoup.parse(h);
        assertEquals("<!doctype html><html><head></head><body>OneTwoThree<link>FourFive Six Seven </body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisesEmptyDocument
    @Test public void normalisesEmptyDocument() {
        Document doc = Jsoup.parse("");
        assertEquals("<html><head></head><body></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisesHeadlessBody
    @Test public void normalisesHeadlessBody() {
        Document doc = Jsoup.parse("<html><body><span class=\"foo\">bar</span>");
        assertEquals("<html><head></head><body><span class=\"foo\">bar</span></body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::normalisedBodyAfterContent
    @Test public void normalisedBodyAfterContent() {
        Document doc = Jsoup.parse("<font face=Arial><body class=name><div>One</div></body></font>");
        assertEquals("<html><head></head><body class=\"name\"><font face=\"Arial\"><div>One</div></font></body></html>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::findsCharsetInMalformedMeta
    @Test public void findsCharsetInMalformedMeta() {
        String h = "<meta http-equiv=Content-Type content=text/html; charset=gb2312>";
        
        Document doc = Jsoup.parse(h);
        assertEquals("gb2312", doc.select("meta").attr("charset"));
    }

// org.jsoup.parser.HtmlParserTest::testHgroup
    @Test public void testHgroup() {
        
        Document doc = Jsoup.parse("<h1>Hello <h2>There <hgroup><h1>Another<h2>headline</hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup>");
        assertEquals("<h1>Hello </h1><h2>There <hgroup><h1>Another</h1><h2>headline</h2></hgroup> <hgroup><h1>More</h1><p>stuff</p></hgroup></h2>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testRelaxedTags
    @Test public void testRelaxedTags() {
        Document doc = Jsoup.parse("<abc_def id=1>Hello</abc_def> <abc-def>There</abc-def>");
        assertEquals("<abc_def id=\"1\">Hello</abc_def> <abc-def>There</abc-def>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testHeaderContents
    @Test public void testHeaderContents() {
        
        
        Document doc = Jsoup.parse("<h1>Hello <div>There</div> now</h1> <h2>More <h3>Content</h3></h2>");
        assertEquals("<h1>Hello <div>There</div> now</h1> <h2>More </h2><h3>Content</h3>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testSpanContents
    @Test public void testSpanContents() {
        
        Document doc = Jsoup.parse("<span>Hello <div>there</div> <span>now</span></span>");
        assertEquals("<span>Hello <div>there</div> <span>now</span></span>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testNoImagesInNoScriptInHead
    @Test public void testNoImagesInNoScriptInHead() {
        
        Document doc = Jsoup.parse("<html><head><noscript><img src='foo'></noscript></head><body><p>Hello</p></body></html>");
        assertEquals("<html><head><noscript>&lt;img src=\"foo\"&gt;</noscript></head><body><p>Hello</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::testAFlowContents
    @Test public void testAFlowContents() {
        
        Document doc = Jsoup.parse("<a>Hello <div>there</div> <span>now</span></a>");
        assertEquals("<a>Hello <div>there</div> <span>now</span></a>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testFontFlowContents
    @Test public void testFontFlowContents() {
        
        Document doc = Jsoup.parse("<font>Hello <div>there</div> <span>now</span></font>");
        assertEquals("<font>Hello <div>there</div> <span>now</span></font>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedTagsBI
    @Test public void handlesMisnestedTagsBI() {
        
        String h = "<p>1<b>2<i>3</b>4</i>5</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p>1<b>2<i>3</i></b><i>4</i>5</p>", doc.body().html());
        
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedTagsBP
    @Test public void handlesMisnestedTagsBP() {
        
        String h = "<b>1<p>2</b>3</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<b>1</b>\n<p><b>2</b>3</p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesMisnestedAInDivs
    @Test public void handlesMisnestedAInDivs() {
        String h = "<a href='#1'><div><div><a href='#2'>child</a</div</div></a>";
        String w = "<a href=\"#1\"></a><div><a href=\"#1\"></a><div><a href=\"#1\"></a><a href=\"#2\">child</a></div></div>";
        Document doc = Jsoup.parse(h);
        assertEquals(
            StringUtil.normaliseWhitespace(w),
            StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesUnexpectedMarkupInTables
    @Test public void handlesUnexpectedMarkupInTables() {
        
        
        String h = "<table><b><tr><td>aaa</td></tr>bbb</table>ccc";
        Document doc = Jsoup.parse(h);
        assertEquals("<b></b><b>bbb</b><table><tbody><tr><td>aaa</td></tr></tbody></table><b>ccc</b>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedFormattingElements
    @Test public void handlesUnclosedFormattingElements() {
        
        String h = "<!DOCTYPE html>\n" +
                "<p><b class=x><b class=x><b><b class=x><b class=x><b>X\n" +
                "<p>X\n" +
                "<p><b><b class=x><b>X\n" +
                "<p></b></b></b></b></b></b>X";
        Document doc = Jsoup.parse(h);
        doc.outputSettings().indentAmount(0);
        String want = "<!doctype html>\n" +
                "<html>\n" +
                "<head></head>\n" +
                "<body>\n" +
                "<p><b class=\"x\"><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></b></p>\n" +
                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b>X </b></b></b></b></b></p>\n" +
                "<p><b class=\"x\"><b><b class=\"x\"><b class=\"x\"><b><b><b class=\"x\"><b>X </b></b></b></b></b></b></b></b></p>\n" +
                "<p>X</p>\n" +
                "</body>\n" +
                "</html>";
        assertEquals(want, doc.html());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedAnchors
    @Test public void handlesUnclosedAnchors() {
        String h = "<a href='http://example.com/'>Link<p>Error link</a>";
        Document doc = Jsoup.parse(h);
        String want = "<a href=\"http://example.com/\">Link</a>\n<p><a href=\"http://example.com/\">Error link</a></p>";
        assertEquals(want, doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::reconstructFormattingElements
    @Test public void reconstructFormattingElements() {
        
        String h = "<p><b class=one>One <i>Two <b>Three</p><p>Hello</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<p><b class=\"one\">One <i>Two <b>Three</b></i></b></p>\n<p><b class=\"one\"><i><b>Hello</b></i></b></p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::reconstructFormattingElementsInTable
    @Test public void reconstructFormattingElementsInTable() {
        
        
        String h = "<p><b>One</p> <table><tr><td><p><i>Three<p>Four</i></td></tr></table> <p>Five</p>";
        Document doc = Jsoup.parse(h);
        String want = "<p><b>One</b></p>\n" +
                "<b> \n" +
                " <table>\n" +
                "  <tbody>\n" +
                "   <tr>\n" +
                "    <td><p><i>Three</i></p><p><i>Four</i></p></td>\n" +
                "   </tr>\n" +
                "  </tbody>\n" +
                " </table> <p>Five</p></b>";
        assertEquals(want, doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::commentBeforeHtml
    @Test public void commentBeforeHtml() {
        String h = "<!-- comment --><!-- comment 2 --><p>One</p>";
        Document doc = Jsoup.parse(h);
        assertEquals("<!-- comment --><!-- comment 2 --><html><head></head><body><p>One</p></body></html>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.HtmlParserTest::emptyTdTag
    @Test public void emptyTdTag() {
        String h = "<table><tr><td>One</td><td id='2' /></tr></table>";
        Document doc = Jsoup.parse(h);
        assertEquals("<td>One</td>\n<td id=\"2\"></td>", doc.select("tr").first().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesSolidusInA
    @Test public void handlesSolidusInA() {
        
        String h = "<a class=lp href=/lib/14160711/>link text</a>";
        Document doc = Jsoup.parse(h);
        Element a = doc.select("a").first();
        assertEquals("link text", a.text());
        assertEquals("/lib/14160711/", a.attr("href"));
    }

// org.jsoup.parser.HtmlParserTest::handlesSpanInTbody
    @Test public void handlesSpanInTbody() {
        
        String h = "<table><tbody><span class='1'><tr><td>One</td></tr><tr><td>Two</td></tr></span></tbody></table>";
        Document doc = Jsoup.parse(h);
        assertEquals(doc.select("span").first().children().size(), 0); 
        assertEquals(doc.select("table").size(), 1); 
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedTitleAtEof
    @Test public void handlesUnclosedTitleAtEof() {
        assertEquals("Data", Jsoup.parse("<title>Data").title());
        assertEquals("Data<", Jsoup.parse("<title>Data<").title());
        assertEquals("Data</", Jsoup.parse("<title>Data</").title());
        assertEquals("Data</t", Jsoup.parse("<title>Data</t").title());
        assertEquals("Data</ti", Jsoup.parse("<title>Data</ti").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title>").title());
        assertEquals("Data", Jsoup.parse("<title>Data</title >").title());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedTitle
    @Test public void handlesUnclosedTitle() {
        Document one = Jsoup.parse("<title>One <b>Two <b>Three</TITLE><p>Test</p>"); 
        assertEquals("One <b>Two <b>Three", one.title());
        assertEquals("Test", one.select("p").first().text());

        Document two = Jsoup.parse("<title>One<b>Two <p>Test</p>"); 
        assertEquals("One", two.title());
        assertEquals("<b>Two <p>Test</p></b>", two.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedScriptAtEof
    @Test public void handlesUnclosedScriptAtEof() {
        assertEquals("Data", Jsoup.parse("<script>Data").select("script").first().data());
        assertEquals("Data<", Jsoup.parse("<script>Data<").select("script").first().data());
        assertEquals("Data</sc", Jsoup.parse("<script>Data</sc").select("script").first().data());
        assertEquals("Data</-sc", Jsoup.parse("<script>Data</-sc").select("script").first().data());
        assertEquals("Data</sc-", Jsoup.parse("<script>Data</sc-").select("script").first().data());
        assertEquals("Data</sc--", Jsoup.parse("<script>Data</sc--").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script>").select("script").first().data());
        assertEquals("Data</script", Jsoup.parse("<script>Data</script").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script ").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=\"").select("script").first().data());
        assertEquals("Data", Jsoup.parse("<script>Data</script n=\"p").select("script").first().data());
    }

// org.jsoup.parser.HtmlParserTest::handlesUnclosedRawtextAtEof
    @Test public void handlesUnclosedRawtextAtEof() {
        assertEquals("Data", Jsoup.parse("<style>Data").select("style").first().data());
        assertEquals("Data</st", Jsoup.parse("<style>Data</st").select("style").first().data());
        assertEquals("Data", Jsoup.parse("<style>Data</style>").select("style").first().data());
        assertEquals("Data</style", Jsoup.parse("<style>Data</style").select("style").first().data());
        assertEquals("Data</-style", Jsoup.parse("<style>Data</-style").select("style").first().data());
        assertEquals("Data</style-", Jsoup.parse("<style>Data</style-").select("style").first().data());
        assertEquals("Data</style--", Jsoup.parse("<style>Data</style--").select("style").first().data());
    }

// org.jsoup.parser.HtmlParserTest::noImplicitFormForTextAreas
    @Test public void noImplicitFormForTextAreas() {
        
        Document doc = Jsoup.parse("<textarea>One</textarea>");
        assertEquals("<textarea>One</textarea>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesEscapedScript
    @Test public void handlesEscapedScript() {
        Document doc = Jsoup.parse("<script><!-- one <script>Blah</script> --></script>");
        assertEquals("<!-- one <script>Blah</script> -->", doc.select("script").first().data());
    }

// org.jsoup.parser.HtmlParserTest::handles0CharacterAsText
    @Test public void handles0CharacterAsText() {
        Document doc = Jsoup.parse("0<p>0</p>");
        assertEquals("0\n<p>0</p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesNullInData
    @Test public void handlesNullInData() {
        Document doc = Jsoup.parse("<p id=\u0000>Blah \u0000</p>");
        assertEquals("<p id=\"\uFFFD\">Blah \u0000</p>", doc.body().html()); 
    }

// org.jsoup.parser.HtmlParserTest::handlesNullInComments
    @Test public void handlesNullInComments() {
        Document doc = Jsoup.parse("<body><!-- \u0000 \u0000 -->");
        assertEquals("<!-- \uFFFD \uFFFD -->", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesNewlinesAndWhitespaceInTag
    @Test public void handlesNewlinesAndWhitespaceInTag() {
        Document doc = Jsoup.parse("<a \n href=\"one\" \r\n id=\"two\" \f >");
        assertEquals("<a href=\"one\" id=\"two\"></a>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesWhitespaceInoDocType
    @Test public void handlesWhitespaceInoDocType() {
        String html = "<!DOCTYPE html\r\n" +
                "      PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\r\n" +
                "      \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
        Document doc = Jsoup.parse(html);
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">", doc.childNode(0).outerHtml());
    }

// org.jsoup.parser.HtmlParserTest::tracksErrorsWhenRequested
    @Test public void tracksErrorsWhenRequested() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(500);
        Document doc = Jsoup.parse(html, "http://example.com", parser);
        
        List<ParseError> errors = parser.getErrors();
        assertEquals(5, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
        assertEquals("50: Tag cannot be self closing; not a void tag", errors.get(3).toString());
        assertEquals("61: Unexpectedly reached end of file (EOF) in input state [TagName]", errors.get(4).toString());
    }

// org.jsoup.parser.HtmlParserTest::tracksLimitedErrorsWhenRequested
    @Test public void tracksLimitedErrorsWhenRequested() {
        String html = "<p>One</p href='no'><!DOCTYPE html>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser().setTrackErrors(3);
        Document doc = parser.parseInput(html, "http://example.com");

        List<ParseError> errors = parser.getErrors();
        assertEquals(3, errors.size());
        assertEquals("20: Attributes incorrectly present on end tag", errors.get(0).toString());
        assertEquals("35: Unexpected token [Doctype] when in state [InBody]", errors.get(1).toString());
        assertEquals("36: Invalid character reference: invalid named referenece 'arrgh'", errors.get(2).toString());
    }

// org.jsoup.parser.HtmlParserTest::noErrorsByDefault
    @Test public void noErrorsByDefault() {
        String html = "<p>One</p href='no'>&arrgh;<font /><br /><foo";
        Parser parser = Parser.htmlParser();
        Document doc = Jsoup.parse(html, "http://example.com", parser);

        List<ParseError> errors = parser.getErrors();
        assertEquals(0, errors.size());
    }

// org.jsoup.parser.HtmlParserTest::handlesCommentsInTable
    @Test public void handlesCommentsInTable() {
        String html = "<table><tr><td>text</td><!-- Comment --></tr></table>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<html><head></head><body><table><tbody><tr><td>text</td><!-- Comment --></tr></tbody></table></body></html>", TextUtil.stripNewlines(node.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesQuotesInCommentsInScripts
    @Test public void handlesQuotesInCommentsInScripts() {
        String html = "<script>\n" +
                "  <!--\n" +
                "    document.write('</scr' + 'ipt>');\n" +
                "  
                "</script>";
        Document node = Jsoup.parseBodyFragment(html);
        assertEquals("<script>\n" +
                "  <!--\n" +
                "    document.write('</scr' + 'ipt>');\n" +
                "  
                "</script>", node.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handleNullContextInParseFragment
    @Test public void handleNullContextInParseFragment() {
        String html = "<ol><li>One</li></ol><p>Two</p>";
        List<Node> nodes = Parser.parseFragment(html, null, "http://example.com/");
        assertEquals(1, nodes.size()); 
        assertEquals("html", nodes.get(0).nodeName());
        assertEquals("<html> <head></head> <body> <ol> <li>One</li> </ol> <p>Two</p> </body> </html>", StringUtil.normaliseWhitespace(nodes.get(0).outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::doesNotFindShortestMatchingEntity
    @Test public void doesNotFindShortestMatchingEntity() {
        
        
        String html = "One &clubsuite; &clubsuit;";
        Document doc = Jsoup.parse(html);
        assertEquals(StringUtil.normaliseWhitespace("One &amp;clubsuite; ♣"), doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::relaxedBaseEntityMatchAndStrictExtendedMatch
    @Test public void relaxedBaseEntityMatchAndStrictExtendedMatch() {
        
        String html = "&amp &quot &reg &icy &hopf &icy; &hopf;";
        Document doc = Jsoup.parse(html);
        doc.outputSettings().escapeMode(Entities.EscapeMode.extended).charset("ascii"); 
        assertEquals("&amp; \" &reg; &amp;icy &amp;hopf &icy; &hopf;", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesXmlDeclarationAsBogusComment
    @Test public void handlesXmlDeclarationAsBogusComment() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body>";
        Document doc = Jsoup.parse(html);
        assertEquals("<!--?xml encoding='UTF-8' ?--> <html> <head></head> <body> One </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesTagsInTextarea
    @Test public void handlesTagsInTextarea() {
        String html = "<textarea><p>Jsoup</p></textarea>";
        Document doc = Jsoup.parse(html);
        assertEquals("<textarea>&lt;p&gt;Jsoup&lt;/p&gt;</textarea>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::createsFormElements
    @Test public void createsFormElements() {
        String html = "<body><form><input id=1><input id=2></form></body>";
        Document doc = Jsoup.parse(html);
        Element el = doc.select("form").first();

        assertTrue("Is form element", el instanceof FormElement);
        FormElement form = (FormElement) el;
        Elements controls = form.elements();
        assertEquals(2, controls.size());
        assertEquals("1", controls.get(0).id());
        assertEquals("2", controls.get(1).id());
    }

// org.jsoup.parser.HtmlParserTest::associatedFormControlsWithDisjointForms
    @Test public void associatedFormControlsWithDisjointForms() {
        
        String html = "<table><tr><form><input type=hidden id=1><td><input type=text id=2></td><tr></table>";
        Document doc = Jsoup.parse(html);
        Element el = doc.select("form").first();

        assertTrue("Is form element", el instanceof FormElement);
        FormElement form = (FormElement) el;
        Elements controls = form.elements();
        assertEquals(2, controls.size());
        assertEquals("1", controls.get(0).id());
        assertEquals("2", controls.get(1).id());

        assertEquals("<table><tbody><tr><form></form><input type=\"hidden\" id=\"1\"><td><input type=\"text\" id=\"2\"></td></tr><tr></tr></tbody></table>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::handlesInputInTable
    @Test public void handlesInputInTable() {
        String h = "<body>\n" +
                "<input type=\"hidden\" name=\"a\" value=\"\">\n" +
                "<table>\n" +
                "<input type=\"hidden\" name=\"b\" value=\"\" />\n" +
                "</table>\n" +
                "</body>";
        Document doc = Jsoup.parse(h);
        assertEquals(1, doc.select("table input").size());
        assertEquals(2, doc.select("input").size());
    }

// org.jsoup.parser.HtmlParserTest::convertsImageToImg
    @Test public void convertsImageToImg() {
        
        String h = "<body><image><svg><image /></svg></body>";
        Document doc = Jsoup.parse(h);
        assertEquals("<img>\n<svg>\n <image />\n</svg>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::handlesInvalidDoctypes
    @Test public void handlesInvalidDoctypes() {
        
        Document doc = Jsoup.parse("<!DOCTYPE>");
        assertEquals(
                "<!doctype> <html> <head></head> <body></body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE><html><p>Foo</p></html>");
        assertEquals(
                "<!doctype> <html> <head></head> <body> <p>Foo</p> </body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));

        doc = Jsoup.parse("<!DOCTYPE \u0000>");
        assertEquals(
                "<!doctype �> <html> <head></head> <body></body> </html>",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesManyChildren
    @Test public void handlesManyChildren() {
        
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<br>");
        }
        
        
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");
        
        
        assertEquals(50000, doc.body().childNodeSize());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }

// org.jsoup.parser.HtmlParserTest::handlesDeepStack
    @Test public void handlesDeepStack() {
        
        

        
        StringBuilder longBody = new StringBuilder(500000);
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("<dl><dd>");
        }
        for (int i = 0; i < 25000; i++) {
            longBody.append(i).append("</dd></dl>");
        }

        
        long start = System.currentTimeMillis();
        Document doc = Parser.parseBodyFragment(longBody.toString(), "");

        
        assertEquals(2, doc.body().childNodeSize());
        assertEquals(25000, doc.select("dd").size());
        assertTrue(System.currentTimeMillis() - start < 1000);
    }

// org.jsoup.parser.HtmlParserTest::testInvalidTableContents
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

// org.jsoup.parser.HtmlParserTest::testNormalisesIsIndex
    @Test public void testNormalisesIsIndex() {
        Document doc = Jsoup.parse("<body><isindex action='/submit'></body>");
        String html = doc.outerHtml();
        assertEquals("<form action=\"/submit\"> <hr> <label>This is a searchable index. Enter search keywords: <input name=\"isindex\"></label> <hr> </form>",
                StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::testReinsertionModeForThCelss
    @Test public void testReinsertionModeForThCelss() {
        String body = "<body> <table> <tr> <th> <table><tr><td></td></tr></table> <div> <table><tr><td></td></tr></table> </div> <div></div> <div></div> <div></div> </th> </tr> </table> </body>";
        Document doc = Jsoup.parse(body);
        assertEquals(1, doc.body().children().size());
    }

// org.jsoup.parser.HtmlParserTest::testUsingSingleQuotesInQueries
    @Test public void testUsingSingleQuotesInQueries() {
        String body = "<body> <div class='main'>hello</div></body>";
        Document doc = Jsoup.parse(body);
        Elements main = doc.select("div[class='main']");
        assertEquals("hello", main.text());
    }

// org.jsoup.parser.HtmlParserTest::testSupportsNonAsciiTags
    @Test public void testSupportsNonAsciiTags() {
        String body = "<進捗推移グラフ>Yes</進捗推移グラフ><русский-тэг>Correct</<русский-тэг>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("進捗推移グラフ");
        assertEquals("Yes", els.text());
        els = doc.select("русский-тэг");
        assertEquals("Correct", els.text());
    }

// org.jsoup.parser.HtmlParserTest::testSupportsPartiallyNonAsciiTags
    @Test public void testSupportsPartiallyNonAsciiTags() {
        String body = "<div>Check</divá>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("div");
        assertEquals("Check", els.text());
    }

// org.jsoup.parser.HtmlParserTest::testFragment
    @Test public void testFragment() {
        
        String html =
            "<script type=\"text/javascript\">console.log('foo');</script>\n" +
                "<div id=\"somecontent\">some content</div>\n" +
                "<script type=\"text/javascript\">console.log('bar');</script>";

        Document body = Jsoup.parseBodyFragment(html);
        assertEquals("<script type=\"text/javascript\">console.log('foo');</script> \n" +
            "<div id=\"somecontent\">\n" +
            " some content\n" +
            "</div> \n" +
            "<script type=\"text/javascript\">console.log('bar');</script>", body.body().html());
    }

// org.jsoup.parser.HtmlParserTest::testHtmlLowerCase
    @Test public void testHtmlLowerCase() {
        String html = "<!doctype HTML><DIV ID=1>One</DIV>";
        Document doc = Jsoup.parse(html);
        assertEquals("<!doctype html> <html> <head></head> <body> <div id=\"1\"> One </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::canPreserveTagCase
    @Test public void canPreserveTagCase() {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(true, false));
        Document doc = parser.parseInput("<div id=1><SPAN ID=2>", "");
        assertEquals("<html> <head></head> <body> <div id=\"1\"> <SPAN id=\"2\"></SPAN> </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::canPreserveAttributeCase
    @Test public void canPreserveAttributeCase() {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(false, true));
        Document doc = parser.parseInput("<div id=1><SPAN ID=2>", "");
        assertEquals("<html> <head></head> <body> <div id=\"1\"> <span ID=\"2\"></span> </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::canPreserveBothCase
    @Test public void canPreserveBothCase() {
        Parser parser = Parser.htmlParser();
        parser.settings(new ParseSettings(true, true));
        Document doc = parser.parseInput("<div id=1><SPAN ID=2>", "");
        assertEquals("<html> <head></head> <body> <div id=\"1\"> <SPAN ID=\"2\"></SPAN> </div> </body> </html>", StringUtil.normaliseWhitespace(doc.outerHtml()));
    }

// org.jsoup.parser.HtmlParserTest::handlesControlCodeInAttributeName
    @Test public void handlesControlCodeInAttributeName() {
        Document doc = Jsoup.parse("<p><a \06=foo>One</a><a/\06=bar><a foo\06=bar>Two</a></p>");
        assertEquals("<p><a>One</a><a></a><a foo=\"bar\">Two</a></p>", doc.body().html());
    }

// org.jsoup.parser.HtmlParserTest::caseSensitiveParseTree
    @Test public void caseSensitiveParseTree() {
        String html = "<r><X>A</X><y>B</y></r>";
        Parser parser = Parser.htmlParser();
        parser.settings(ParseSettings.preserveCase);
        Document doc = parser.parseInput(html, "");
        assertEquals("<r> <X> A </X> <y> B </y> </r>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::caseInsensitiveParseTree
    @Test public void caseInsensitiveParseTree() {
        String html = "<r><X>A</X><y>B</y></r>";
        Parser parser = Parser.htmlParser();
        Document doc = parser.parseInput(html, "");
        assertEquals("<r> <x> A </x> <y> B </y> </r>", StringUtil.normaliseWhitespace(doc.body().html()));
    }

// org.jsoup.parser.HtmlParserTest::selfClosingVoidIsNotAnError
    @Test public void selfClosingVoidIsNotAnError() {
        String html = "<p>test<br/>test<br/></p>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput(html, "");
        assertEquals(0, parser.getErrors().size());

        assertTrue(Jsoup.isValid(html, Whitelist.basic()));
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<p>test<br>test<br></p>", clean);
    }

// org.jsoup.parser.HtmlParserTest::selfClosingOnNonvoidIsError
    @Test public void selfClosingOnNonvoidIsError() {
        String html = "<p>test</p><div /><div>Two</div>";
        Parser parser = Parser.htmlParser().setTrackErrors(5);
        parser.parseInput(html, "");
        assertEquals(1, parser.getErrors().size());
        assertEquals("18: Tag cannot be self closing; not a void tag", parser.getErrors().get(0).toString());

        assertFalse(Jsoup.isValid(html, Whitelist.relaxed()));
        String clean = Jsoup.clean(html, Whitelist.relaxed());
        assertEquals("<p>test</p> <div></div> <div> Two </div>", StringUtil.normaliseWhitespace(clean));
    }

// org.jsoup.parser.HtmlParserTest::testTemplateInsideTable
  @Test public void testTemplateInsideTable() throws IOException {
        File in = ParseTest.getFile("/htmltests/table-polymer-template.html");
        Document doc = Jsoup.parse(in, "UTF-8");
        doc.outputSettings().prettyPrint(true);

        Elements templates = doc.body().getElementsByTag("template");
        for (Element template : templates) {
            assertTrue(template.childNodes().size() > 1);
        }
  }

// org.jsoup.parser.HtmlParserTest::testHandlesDeepSpans
  @Test public void testHandlesDeepSpans() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("<span>");
        }

        sb.append("<p>One</p>");

        Document doc = Jsoup.parse(sb.toString());
        assertEquals(200, doc.select("span").size());
        assertEquals(1, doc.select("p").size());
  }

// org.jsoup.parser.HtmlTreeBuilderTest::ensureSearchArraysAreSorted
    public void ensureSearchArraysAreSorted() {
        String[][] arrays = {
            HtmlTreeBuilder.TagsSearchInScope,
            HtmlTreeBuilder.TagSearchList,
            HtmlTreeBuilder.TagSearchButton,
            HtmlTreeBuilder.TagSearchTableScope,
            HtmlTreeBuilder.TagSearchSelectScope,
            HtmlTreeBuilder.TagSearchEndTags,
            HtmlTreeBuilder.TagSearchSpecial
        };

        for (String[] array : arrays) {
            String[] copy = Arrays.copyOf(array, array.length);
            Arrays.sort(array);
            assertArrayEquals(array, copy);
        }
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSimpleXmlParse
    public void testSimpleXmlParse() {
        String xml = "<doc id=2 href='/bar'>Foo <br /><link>One</link><link>Two</link></doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<doc id=\"2\" href=\"/bar\">Foo <br /><link>One</link><link>Two</link></doc>",
                TextUtil.stripNewlines(doc.html()));
        assertEquals(doc.getElementById("2").absUrl("href"), "http://foo.com/bar");
    }

// org.jsoup.parser.XmlTreeBuilderTest::testPopToClose
    public void testPopToClose() {
        
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testCommentAndDocType
    public void testCommentAndDocType() {
        String xml = "<!DOCTYPE HTML><!-- a comment -->One <qux />Two";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        Document doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<!DOCTYPE HTML><!-- a comment -->One <qux />Two",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToJsoupClass
    public void testSupplyParserToJsoupClass() {
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        Document doc = Jsoup.parse(xml, "http://foo.com/", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToConnection
    public void testSupplyParserToConnection() throws IOException {
        String xmlUrl = "http://direct.infohound.net/tools/jsoup-xml-test.xml";

        
        Document xmlDoc = Jsoup.connect(xmlUrl).parser(Parser.xmlParser()).get();
        Document htmlDoc = Jsoup.connect(xmlUrl).parser(Parser.htmlParser()).get();
        Document autoXmlDoc = Jsoup.connect(xmlUrl).get(); 

        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(xmlDoc.html()));
        assertFalse(htmlDoc.equals(xmlDoc));
        assertEquals(xmlDoc, autoXmlDoc);
        assertEquals(1, htmlDoc.select("head").size()); 
        assertEquals(0, xmlDoc.select("head").size()); 
        assertEquals(0, autoXmlDoc.select("head").size()); 
    }

// org.jsoup.parser.XmlTreeBuilderTest::testSupplyParserToDataStream
    public void testSupplyParserToDataStream() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-test.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        Document doc = Jsoup.parse(inStream, null, "http://foo.com", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDoesNotForceSelfClosingKnownTags
    public void testDoesNotForceSelfClosingKnownTags() {
        
        Document htmlDoc = Jsoup.parse("<br>one</br>");
        assertEquals("<br>one\n<br>", htmlDoc.body().html());

        Document xmlDoc = Jsoup.parse("<br>one</br>", "", Parser.xmlParser());
        assertEquals("<br>one</br>", xmlDoc.html());
    }

// org.jsoup.parser.XmlTreeBuilderTest::handlesXmlDeclarationAsDeclaration
    @Test public void handlesXmlDeclarationAsDeclaration() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<?xml encoding=\"UTF-8\"?> <body> One </body> <!-- comment -->",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertEquals("#comment", doc.childNode(2).nodeName());
    }

// org.jsoup.parser.XmlTreeBuilderTest::xmlFragment
    @Test public void xmlFragment() {
        String xml = "<one src='/foo/' />Two<three><four /></three>";
        List<Node> nodes = Parser.parseXmlFragment(xml, "http://example.com/");
        assertEquals(3, nodes.size());

        assertEquals("http://example.com/foo/", nodes.get(0).absUrl("src"));
        assertEquals("one", nodes.get(0).nodeName());
        assertEquals("Two", ((TextNode)nodes.get(1)).text());
    }

// org.jsoup.parser.XmlTreeBuilderTest::xmlParseDefaultsToHtmlOutputSyntax
    @Test public void xmlParseDefaultsToHtmlOutputSyntax() {
        Document doc = Jsoup.parse("x", "", Parser.xmlParser());
        assertEquals(Syntax.xml, doc.outputSettings().syntax());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDoesHandleEOFInTag
    public void testDoesHandleEOFInTag() {
        String html = "<img src=asdf onerror=\"alert(1)\" x=";
        Document xmlDoc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<img src=\"asdf\" onerror=\"alert(1)\" x=\"\" />", xmlDoc.html());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testDetectCharsetEncodingDeclaration
    public void testDetectCharsetEncodingDeclaration() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-charset.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        Document doc = Jsoup.parse(inStream, null, "http://example.com/", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.charset().name());
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <data>äöåéü</data>",
            TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::testParseDeclarationAttributes
    public void testParseDeclarationAttributes() {
        String xml = "<?xml version='1' encoding='UTF-8' something='else'?><val>One</val>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        assertEquals("1", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("else", decl.attr("something"));
        assertEquals("version=\"1\" encoding=\"UTF-8\" something=\"else\"", decl.getWholeDeclaration());
        assertEquals("<?xml version=\"1\" encoding=\"UTF-8\" something=\"else\"?>", decl.outerHtml());
    }

// org.jsoup.parser.XmlTreeBuilderTest::caseSensitiveDeclaration
    public void caseSensitiveDeclaration() {
        String xml = "<?XML version='1' encoding='UTF-8' something='else'?>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<?XML version=\"1\" encoding=\"UTF-8\" something=\"else\"?>", doc.outerHtml());
    }

// org.jsoup.parser.XmlTreeBuilderTest::testCreatesValidProlog
    public void testCreatesValidProlog() {
        Document document = Document.createShell("");
        document.outputSettings().syntax(Syntax.xml);
        document.charset(Charset.forName("utf-8"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<html>\n" +
            " <head></head>\n" +
            " <body></body>\n" +
            "</html>", document.outerHtml());
    }

// org.jsoup.parser.XmlTreeBuilderTest::preservesCaseByDefault
    public void preservesCaseByDefault() {
        String xml = "<TEST ID=1>Check</TEST>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<TEST ID=\"1\">Check</TEST>", TextUtil.stripNewlines(doc.html()));
    }

// org.jsoup.parser.XmlTreeBuilderTest::canNormalizeCase
    public void canNormalizeCase() {
        String xml = "<TEST ID=1>Check</TEST>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser().settings(ParseSettings.htmlDefault));
        assertEquals("<test id=\"1\">Check</test>", TextUtil.stripNewlines(doc.html()));
    }

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
        assertEquals("<p><img src=\"http://example.com/\" alt=\"Image\"></p><p><img></p>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRelaxed
    @Test public void testRelaxed() {
        String h = "<h1>Head</h1><table><tr><td>One<td>Two</td></tr></table>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<h1>Head</h1><table><tbody><tr><td>One</td><td>Two</td></tr></tbody></table>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveTags
    @Test public void testRemoveTags() {
        String h = "<div><p><A HREF='HTTP://nice.com'>Nice</a></p><blockquote>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeTags("a"));

        assertEquals("<p>Nice</p><blockquote>Hello</blockquote>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveAttributes
    @Test public void testRemoveAttributes() {
        String h = "<div><p>Nice</p><blockquote cite='http://example.com/quotations'>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeAttributes("blockquote", "cite"));

        assertEquals("<p>Nice</p><blockquote>Hello</blockquote>", TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveEnforcedAttributes
    @Test public void testRemoveEnforcedAttributes() {
        String h = "<div><p><A HREF='HTTP://nice.com'>Nice</a></p><blockquote>Hello</blockquote>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeEnforcedAttribute("a", "rel"));

        assertEquals("<p><a href=\"http://nice.com\">Nice</a></p><blockquote>Hello</blockquote>",
                TextUtil.stripNewlines(cleanHtml));
    }

// org.jsoup.safety.CleanerTest::testRemoveProtocols
    @Test public void testRemoveProtocols() {
        String h = "<p>Contact me <a href='mailto:info@example.com'>here</a></p>";
        String cleanHtml = Jsoup.clean(h, Whitelist.basic().removeProtocols("a", "href", "ftp", "mailto"));

        assertEquals("<p>Contact me <a rel=\"nofollow\">here</a></p>",
                TextUtil.stripNewlines(cleanHtml));
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
        assertEquals("<img>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testCleanJavascriptHref
    @Test public void testCleanJavascriptHref() {
        String h = "<A HREF=\"javascript:document.location='http://www.google.com/'\">XSS</A>";
        String cleanHtml = Jsoup.clean(h, Whitelist.relaxed());
        assertEquals("<a>XSS</a>", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testCleanAnchorProtocol
    @Test public void testCleanAnchorProtocol() {
        String validAnchor = "<a href=\"#valid\">Valid anchor</a>";
        String invalidAnchor = "<a href=\"#anchor with spaces\">Invalid anchor</a>";

        
        String cleanHtml = Jsoup.clean(validAnchor, Whitelist.relaxed());
        assertEquals("<a>Valid anchor</a>", cleanHtml);

        cleanHtml = Jsoup.clean(invalidAnchor, Whitelist.relaxed());
        assertEquals("<a>Invalid anchor</a>", cleanHtml);

        
        Whitelist relaxedWithAnchor = Whitelist.relaxed().addProtocols("a", "href", "#");

        cleanHtml = Jsoup.clean(validAnchor, relaxedWithAnchor);
        assertEquals(validAnchor, cleanHtml);

        
        cleanHtml = Jsoup.clean(invalidAnchor, relaxedWithAnchor);
        assertEquals("<a>Invalid anchor</a>", cleanHtml);
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
        assertEquals("<img alt=\"\">", cleanHtml);
    }

// org.jsoup.safety.CleanerTest::testIsValidBodyHtml
    @Test public void testIsValidBodyHtml() {
        String ok = "<p>Test <b><a href='http://example.com/' rel='nofollow'>OK</a></b></p>";
        String ok1 = "<p>Test <b><a href='http://example.com/'>OK</a></b></p>"; 
        String nok1 = "<p><script></script>Not <b>OK</b></p>";
        String nok2 = "<p align=right>Test Not <b>OK</b></p>";
        String nok3 = "<!-- comment --><p>Not OK</p>"; 
        String nok4 = "<html><head>Foo</head><body><b>OK</b></body></html>"; 
        String nok5 = "<p>Test <b><a href='http://example.com/' rel='nofollowme'>OK</a></b></p>";
        String nok6 = "<p>Test <b><a href='http://example.com/'>OK</b></p>"; 
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

// org.jsoup.safety.CleanerTest::testIsValidDocument
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

// org.jsoup.safety.CleanerTest::resolvesRelativeLinks
    @Test public void resolvesRelativeLinks() {
        String html = "<a href='/foo'>Link</a><img src='/bar'>";
        String clean = Jsoup.clean(html, "http://example.com/", Whitelist.basicWithImages());
        assertEquals("<a href=\"http://example.com/foo\" rel=\"nofollow\">Link</a>\n<img src=\"http://example.com/bar\">", clean);
    }

// org.jsoup.safety.CleanerTest::preservesRelativeLinksIfConfigured
    @Test public void preservesRelativeLinksIfConfigured() {
        String html = "<a href='/foo'>Link</a><img src='/bar'> <img src='javascript:alert()'>";
        String clean = Jsoup.clean(html, "http://example.com/", Whitelist.basicWithImages().preserveRelativeLinks(true));
        assertEquals("<a href=\"/foo\" rel=\"nofollow\">Link</a>\n<img src=\"/bar\"> \n<img>", clean);
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
        assertEquals("<img> \n<img>", dropped);

        String preserved = Jsoup.clean(html, Whitelist.basicWithImages().addProtocols("img", "src", "cid", "data"));
        assertEquals("<img src=\"cid:12345\"> \n<img src=\"data:gzzt\">", preserved);
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
        os.charset("ascii");

        String html = "<div><p>&bernou;</p></div>";
        String customOut = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed(), os);
        String defaultOut = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed());
        assertNotSame(defaultOut, customOut);

        assertEquals("<div><p>&Bscr;</p></div>", customOut); 
        assertEquals("<div>\n" +
            " <p>ℬ</p>\n" +
            "</div>", defaultOut);

        os.charset("ASCII");
        os.escapeMode(Entities.EscapeMode.base);
        String customOut2 = Jsoup.clean(html, "http://foo.com/", Whitelist.relaxed(), os);
        assertEquals("<div><p>&#x212c;</p></div>", customOut2);
    }

// org.jsoup.safety.CleanerTest::handlesFramesets
    @Test public void handlesFramesets() {
        String dirty = "<html><head><script></script><noscript></noscript></head><frameset><frame src=\"foo\" /><frame src=\"foo\" /></frameset></html>";
        String clean = Jsoup.clean(dirty, Whitelist.basic());
        assertEquals("", clean); 

        Document dirtyDoc = Jsoup.parse(dirty);
        Document cleanDoc = new Cleaner(Whitelist.basic()).clean(dirtyDoc);
        assertFalse(cleanDoc == null);
        assertEquals(0, cleanDoc.body().childNodeSize());
    }

// org.jsoup.safety.CleanerTest::cleansInternationalText
    @Test public void cleansInternationalText() {
        assertEquals("привет", Jsoup.clean("привет", Whitelist.none()));
    }

// org.jsoup.safety.CleanerTest::testScriptTagInWhiteList
    public void testScriptTagInWhiteList() {
        Whitelist whitelist = Whitelist.relaxed();
        whitelist.addTags( "script" );
        assertTrue( Jsoup.isValid("Hello<script>alert('Doh')</script>World !", whitelist ) );
    }

// org.jsoup.safety.CleanerTest::bailsIfRemovingProtocolThatsNotSet
    public void bailsIfRemovingProtocolThatsNotSet() {
        
        Whitelist w = Whitelist.none();

        
        w.addAttributes("a", "href");
        w.removeProtocols("a", "href", "javascript"); 
    }

// org.jsoup.safety.CleanerTest::handlesControlCharactersAfterTagName
    @Test public void handlesControlCharactersAfterTagName() {
        String html = "<a/\06>";
        String clean = Jsoup.clean(html, Whitelist.basic());
        assertEquals("<a rel=\"nofollow\"></a>", clean);
    }

// org.jsoup.select.CssTest::firstChild
	public void firstChild() {
		check(html.select("#pseudo :first-child"), "1");
		check(html.select("html:first-child"));
	}

// org.jsoup.select.CssTest::lastChild
	public void lastChild() {
		check(html.select("#pseudo :last-child"), "10");
		check(html.select("html:last-child"));
	}

// org.jsoup.select.CssTest::nthChild_simple
	public void nthChild_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#pseudo :nth-child(%d)", i)), String.valueOf(i));
		}
	}

// org.jsoup.select.CssTest::nthOfType_unknownTag
    public void nthOfType_unknownTag() {
        for(int i = 1; i <=10; i++) {
            check(html.select(String.format("#type svg:nth-of-type(%d)", i)), String.valueOf(i));
        }
    }

// org.jsoup.select.CssTest::nthLastChild_simple
	public void nthLastChild_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#pseudo :nth-last-child(%d)", i)), String.valueOf(11-i));
		}
	}

// org.jsoup.select.CssTest::nthOfType_simple
	public void nthOfType_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#type p:nth-of-type(%d)", i)), String.valueOf(i));
		}
	}

// org.jsoup.select.CssTest::nthLastOfType_simple
	public void nthLastOfType_simple() {
		for(int i = 1; i <=10; i++) {
			check(html.select(String.format("#type :nth-last-of-type(%d)", i)), String.valueOf(11-i),String.valueOf(11-i),String.valueOf(11-i),String.valueOf(11-i));
		}
	}

// org.jsoup.select.CssTest::nthChild_advanced
	public void nthChild_advanced() {
		check(html.select("#pseudo :nth-child(-5)"));
		check(html.select("#pseudo :nth-child(odd)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(2n-1)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(2n+1)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(2n+3)"), "3", "5", "7", "9");
		check(html.select("#pseudo :nth-child(even)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-child(2n)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-child(3n-1)"), "2", "5", "8");
		check(html.select("#pseudo :nth-child(-2n+5)"), "1", "3", "5");
		check(html.select("#pseudo :nth-child(+5)"), "5");
	}

// org.jsoup.select.CssTest::nthOfType_advanced
	public void nthOfType_advanced() {
		check(html.select("#type :nth-of-type(-5)"));
		check(html.select("#type p:nth-of-type(odd)"), "1", "3", "5", "7", "9");
		check(html.select("#type em:nth-of-type(2n-1)"), "1", "3", "5", "7", "9");
		check(html.select("#type p:nth-of-type(2n+1)"), "1", "3", "5", "7", "9");
		check(html.select("#type span:nth-of-type(2n+3)"), "3", "5", "7", "9");
		check(html.select("#type p:nth-of-type(even)"), "2", "4", "6", "8", "10");
		check(html.select("#type p:nth-of-type(2n)"), "2", "4", "6", "8", "10");
		check(html.select("#type p:nth-of-type(3n-1)"), "2", "5", "8");
		check(html.select("#type p:nth-of-type(-2n+5)"), "1", "3", "5");
		check(html.select("#type :nth-of-type(+5)"), "5", "5", "5", "5");
	}

// org.jsoup.select.CssTest::nthLastChild_advanced
	public void nthLastChild_advanced() {
		check(html.select("#pseudo :nth-last-child(-5)"));
		check(html.select("#pseudo :nth-last-child(odd)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(2n-1)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(2n+1)"), "2", "4", "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(2n+3)"), "2", "4", "6", "8");
		check(html.select("#pseudo :nth-last-child(even)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-last-child(2n)"), "1", "3", "5", "7", "9");
		check(html.select("#pseudo :nth-last-child(3n-1)"), "3", "6", "9");

		check(html.select("#pseudo :nth-last-child(-2n+5)"), "6", "8", "10");
		check(html.select("#pseudo :nth-last-child(+5)"), "6");
	}

// org.jsoup.select.CssTest::nthLastOfType_advanced
	public void nthLastOfType_advanced() {
		check(html.select("#type :nth-last-of-type(-5)"));
		check(html.select("#type p:nth-last-of-type(odd)"), "2", "4", "6", "8", "10");
		check(html.select("#type em:nth-last-of-type(2n-1)"), "2", "4", "6", "8", "10");
		check(html.select("#type p:nth-last-of-type(2n+1)"), "2", "4", "6", "8", "10");
		check(html.select("#type span:nth-last-of-type(2n+3)"), "2", "4", "6", "8");
		check(html.select("#type p:nth-last-of-type(even)"), "1", "3", "5", "7", "9");
		check(html.select("#type p:nth-last-of-type(2n)"), "1", "3", "5", "7", "9");
		check(html.select("#type p:nth-last-of-type(3n-1)"), "3", "6", "9");

		check(html.select("#type span:nth-last-of-type(-2n+5)"), "6", "8", "10");
		check(html.select("#type :nth-last-of-type(+5)"), "6", "6", "6", "6");
	}

// org.jsoup.select.CssTest::firstOfType
	public void firstOfType() {
		check(html.select("div:not(#only) :first-of-type"), "1", "1", "1", "1", "1");
	}

// org.jsoup.select.CssTest::lastOfType
	public void lastOfType() {
		check(html.select("div:not(#only) :last-of-type"), "10", "10", "10", "10", "10");
	}

// org.jsoup.select.CssTest::empty
	public void empty() {
		final Elements sel = html.select(":empty");
		assertEquals(3, sel.size());
		assertEquals("head", sel.get(0).tagName());
		assertEquals("br", sel.get(1).tagName());
		assertEquals("p", sel.get(2).tagName());
	}

// org.jsoup.select.CssTest::onlyChild
	public void onlyChild() {
		final Elements sel = html.select("span :only-child");
		assertEquals(1, sel.size());
		assertEquals("br", sel.get(0).tagName());
		
		check(html.select("#only :only-child"), "only");
	}

// org.jsoup.select.CssTest::onlyOfType
	public void onlyOfType() {
		final Elements sel = html.select(":only-of-type");
		assertEquals(6, sel.size());
		assertEquals("head", sel.get(0).tagName());
		assertEquals("body", sel.get(1).tagName());
		assertEquals("span", sel.get(2).tagName());
		assertEquals("br", sel.get(3).tagName());
		assertEquals("p", sel.get(4).tagName());
		assertTrue(sel.get(4).hasClass("empty"));
		assertEquals("em", sel.get(5).tagName());
	}

// org.jsoup.select.CssTest::root
	public void root() {
		Elements sel = html.select(":root");
		assertEquals(1, sel.size());
		assertNotNull(sel.get(0));
		assertEquals(Tag.valueOf("html"), sel.get(0).tag());

		Elements sel2 = html.select("body").select(":root");
		assertEquals(1, sel2.size());
		assertNotNull(sel2.get(0));
		assertEquals(Tag.valueOf("body"), sel2.get(0).tag());
	}

// org.jsoup.select.ElementsTest::filter
    @Test public void filter() {
        String h = "<p>Excl</p><div class=headline><p>Hello</p><p>There</p></div><div class=headline><h1>Headline</h1></div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select(".headline").select("p");
        assertEquals(2, els.size());
        assertEquals("Hello", els.get(0).text());
        assertEquals("There", els.get(1).text());
    }

// org.jsoup.select.ElementsTest::attributes
    @Test public void attributes() {
        String h = "<p title=foo><p title=bar><p class=foo><p class=bar>";
        Document doc = Jsoup.parse(h);
        Elements withTitle = doc.select("p[title]");
        assertEquals(2, withTitle.size());
        assertTrue(withTitle.hasAttr("title"));
        assertFalse(withTitle.hasAttr("class"));
        assertEquals("foo", withTitle.attr("title"));

        withTitle.removeAttr("title");
        assertEquals(2, withTitle.size()); 
        assertEquals(0, doc.select("p[title]").size());

        Elements ps = doc.select("p").attr("style", "classy");
        assertEquals(4, ps.size());
        assertEquals("classy", ps.last().attr("style"));
        assertEquals("bar", ps.last().attr("class"));
    }

// org.jsoup.select.ElementsTest::hasAttr
    @Test public void hasAttr() {
        Document doc = Jsoup.parse("<p title=foo><p title=bar><p class=foo><p class=bar>");
        Elements ps = doc.select("p");
        assertTrue(ps.hasAttr("class"));
        assertFalse(ps.hasAttr("style"));
    }

// org.jsoup.select.ElementsTest::hasAbsAttr
    @Test public void hasAbsAttr() {
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='https://jsoup.org'>Two</a>");
        Elements one = doc.select("#1");
        Elements two = doc.select("#2");
        Elements both = doc.select("a");
        assertFalse(one.hasAttr("abs:href"));
        assertTrue(two.hasAttr("abs:href"));
        assertTrue(both.hasAttr("abs:href")); 
    }

// org.jsoup.select.ElementsTest::attr
    @Test public void attr() {
        Document doc = Jsoup.parse("<p title=foo><p title=bar><p class=foo><p class=bar>");
        String classVal = doc.select("p").attr("class");
        assertEquals("foo", classVal);
    }

// org.jsoup.select.ElementsTest::absAttr
    @Test public void absAttr() {
        Document doc = Jsoup.parse("<a id=1 href='/foo'>One</a> <a id=2 href='https://jsoup.org'>Two</a>");
        Elements one = doc.select("#1");
        Elements two = doc.select("#2");
        Elements both = doc.select("a");

        assertEquals("", one.attr("abs:href"));
        assertEquals("https://jsoup.org", two.attr("abs:href"));
        assertEquals("https://jsoup.org", both.attr("abs:href"));
    }

// org.jsoup.select.ElementsTest::classes
    @Test public void classes() {
        Document doc = Jsoup.parse("<div><p class='mellow yellow'></p><p class='red green'></p>");

        Elements els = doc.select("p");
        assertTrue(els.hasClass("red"));
        assertFalse(els.hasClass("blue"));
        els.addClass("blue");
        els.removeClass("yellow");
        els.toggleClass("mellow");

        assertEquals("blue", els.get(0).className());
        assertEquals("red green blue mellow", els.get(1).className());
    }

// org.jsoup.select.ElementsTest::hasClassCaseInsensitive
    @Test public void hasClassCaseInsensitive() {
        Elements els = Jsoup.parse("<p Class=One>One <p class=Two>Two <p CLASS=THREE>THREE").select("p");
        Element one = els.get(0);
        Element two = els.get(1);
        Element thr = els.get(2);

        assertTrue(one.hasClass("One"));
        assertTrue(one.hasClass("ONE"));

        assertTrue(two.hasClass("TWO"));
        assertTrue(two.hasClass("Two"));

        assertTrue(thr.hasClass("ThreE"));
        assertTrue(thr.hasClass("three"));
    }

// org.jsoup.select.ElementsTest::text
    @Test public void text() {
        String h = "<div><p>Hello<p>there<p>world</div>";
        Document doc = Jsoup.parse(h);
        assertEquals("Hello there world", doc.select("div > *").text());
    }

// org.jsoup.select.ElementsTest::hasText
    @Test public void hasText() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div><p></p></div>");
        Elements divs = doc.select("div");
        assertTrue(divs.hasText());
        assertFalse(doc.select("div + div").hasText());
    }

// org.jsoup.select.ElementsTest::html
    @Test public void html() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div><p>There</p></div>");
        Elements divs = doc.select("div");
        assertEquals("<p>Hello</p>\n<p>There</p>", divs.html());
    }

// org.jsoup.select.ElementsTest::outerHtml
    @Test public void outerHtml() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div><p>There</p></div>");
        Elements divs = doc.select("div");
        assertEquals("<div><p>Hello</p></div><div><p>There</p></div>", TextUtil.stripNewlines(divs.outerHtml()));
    }

// org.jsoup.select.ElementsTest::setHtml
    @Test public void setHtml() {
        Document doc = Jsoup.parse("<p>One</p><p>Two</p><p>Three</p>");
        Elements ps = doc.select("p");
        
        ps.prepend("<b>Bold</b>").append("<i>Ital</i>");
        assertEquals("<p><b>Bold</b>Two<i>Ital</i></p>", TextUtil.stripNewlines(ps.get(1).outerHtml()));
        
        ps.html("<span>Gone</span>");
        assertEquals("<p><span>Gone</span></p>", TextUtil.stripNewlines(ps.get(1).outerHtml()));
    }

// org.jsoup.select.ElementsTest::val
    @Test public void val() {
        Document doc = Jsoup.parse("<input value='one' /><textarea>two</textarea>");
        Elements els = doc.select("input, textarea");
        assertEquals(2, els.size());
        assertEquals("one", els.val());
        assertEquals("two", els.last().val());
        
        els.val("three");
        assertEquals("three", els.first().val());
        assertEquals("three", els.last().val());
        assertEquals("<textarea>three</textarea>", els.last().outerHtml());
    }

// org.jsoup.select.ElementsTest::before
    @Test public void before() {
        Document doc = Jsoup.parse("<p>This <a>is</a> <a>jsoup</a>.</p>");
        doc.select("a").before("<span>foo</span>");
        assertEquals("<p>This <span>foo</span><a>is</a> <span>foo</span><a>jsoup</a>.</p>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::after
    @Test public void after() {
        Document doc = Jsoup.parse("<p>This <a>is</a> <a>jsoup</a>.</p>");
        doc.select("a").after("<span>foo</span>");
        assertEquals("<p>This <a>is</a><span>foo</span> <a>jsoup</a><span>foo</span>.</p>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::wrap
    @Test public void wrap() {
        String h = "<p><b>This</b> is <b>jsoup</b></p>";
        Document doc = Jsoup.parse(h);
        doc.select("b").wrap("<i></i>");
        assertEquals("<p><i><b>This</b></i> is <i><b>jsoup</b></i></p>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::wrapDiv
    @Test public void wrapDiv() {
        String h = "<p><b>This</b> is <b>jsoup</b>.</p> <p>How do you like it?</p>";
        Document doc = Jsoup.parse(h);
        doc.select("p").wrap("<div></div>");
        assertEquals("<div><p><b>This</b> is <b>jsoup</b>.</p></div> <div><p>How do you like it?</p></div>",
                TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::unwrap
    @Test public void unwrap() {
        String h = "<div><font>One</font> <font><a href=\"/\">Two</a></font></div";
        Document doc = Jsoup.parse(h);
        doc.select("font").unwrap();
        assertEquals("<div>One <a href=\"/\">Two</a></div>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::unwrapP
    @Test public void unwrapP() {
        String h = "<p><a>One</a> Two</p> Three <i>Four</i> <p>Fix <i>Six</i></p>";
        Document doc = Jsoup.parse(h);
        doc.select("p").unwrap();
        assertEquals("<a>One</a> Two Three <i>Four</i> Fix <i>Six</i>", TextUtil.stripNewlines(doc.body().html()));
    }

// org.jsoup.select.ElementsTest::unwrapKeepsSpace
    @Test public void unwrapKeepsSpace() {
        String h = "<p>One <span>two</span> <span>three</span> four</p>";
        Document doc = Jsoup.parse(h);
        doc.select("span").unwrap();
        assertEquals("<p>One two three four</p>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::empty
    @Test public void empty() {
        Document doc = Jsoup.parse("<div><p>Hello <b>there</b></p> <p>now!</p></div>");
        doc.outputSettings().prettyPrint(false);

        doc.select("p").empty();
        assertEquals("<div><p></p> <p></p></div>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::remove
    @Test public void remove() {
        Document doc = Jsoup.parse("<div><p>Hello <b>there</b></p> jsoup <p>now!</p></div>");
        doc.outputSettings().prettyPrint(false);
        
        doc.select("p").remove();
        assertEquals("<div> jsoup </div>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::eq
    @Test public void eq() {
        String h = "<p>Hello<p>there<p>world";
        Document doc = Jsoup.parse(h);
        assertEquals("there", doc.select("p").eq(1).text());
        assertEquals("there", doc.select("p").get(1).text());
    }

// org.jsoup.select.ElementsTest::is
    @Test public void is() {
        String h = "<p>Hello<p title=foo>there<p>world";
        Document doc = Jsoup.parse(h);
        Elements ps = doc.select("p");
        assertTrue(ps.is("[title=foo]"));
        assertFalse(ps.is("[title=bar]"));
    }

// org.jsoup.select.ElementsTest::parents
    @Test public void parents() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><p>There</p>");
        Elements parents = doc.select("p").parents();

        assertEquals(3, parents.size());
        assertEquals("div", parents.get(0).tagName());
        assertEquals("body", parents.get(1).tagName());
        assertEquals("html", parents.get(2).tagName());
    }

// org.jsoup.select.ElementsTest::not
    @Test public void not() {
        Document doc = Jsoup.parse("<div id=1><p>One</p></div> <div id=2><p><span>Two</span></p></div>");

        Elements div1 = doc.select("div").not(":has(p > span)");
        assertEquals(1, div1.size());
        assertEquals("1", div1.first().id());

        Elements div2 = doc.select("div").not("#1");
        assertEquals(1, div2.size());
        assertEquals("2", div2.first().id());
    }

// org.jsoup.select.ElementsTest::tagNameSet
    @Test public void tagNameSet() {
        Document doc = Jsoup.parse("<p>Hello <i>there</i> <i>now</i></p>");
        doc.select("i").tagName("em");

        assertEquals("<p>Hello <em>there</em> <em>now</em></p>", doc.body().html());
    }

// org.jsoup.select.ElementsTest::traverse
    @Test public void traverse() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        doc.select("div").traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
            }

            public void tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
            }
        });
        assertEquals("<div><p><#text></#text></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.ElementsTest::forms
    @Test public void forms() {
        Document doc = Jsoup.parse("<form id=1><input name=q></form><div /><form id=2><input name=f></form>");
        Elements els = doc.select("*");
        assertEquals(9, els.size());

        List<FormElement> forms = els.forms();
        assertEquals(2, forms.size());
        assertTrue(forms.get(0) != null);
        assertTrue(forms.get(1) != null);
        assertEquals("1", forms.get(0).id());
        assertEquals("2", forms.get(1).id());
    }

// org.jsoup.select.ElementsTest::classWithHyphen
    @Test public void classWithHyphen() {
        Document doc = Jsoup.parse("<p class='tab-nav'>Check</p>");
        Elements els = doc.getElementsByClass("tab-nav");
        assertEquals(1, els.size());
        assertEquals("Check", els.text());
    }

// org.jsoup.select.ElementsTest::siblings
    @Test public void siblings() {
        Document doc = Jsoup.parse("<div><p>1<p>2<p>3<p>4<p>5<p>6</div><div><p>7<p>8<p>9<p>10<p>11<p>12</div>");

        Elements els = doc.select("p:eq(3)"); 
        assertEquals(2, els.size());

        Elements next = els.next();
        assertEquals(2, next.size());
        assertEquals("5", next.first().text());
        assertEquals("11", next.last().text());

        assertEquals(0, els.next("p:contains(6)").size());
        final Elements nextF = els.next("p:contains(5)");
        assertEquals(1, nextF.size());
        assertEquals("5", nextF.first().text());

        Elements nextA = els.nextAll();
        assertEquals(4, nextA.size());
        assertEquals("5", nextA.first().text());
        assertEquals("12", nextA.last().text());

        Elements nextAF = els.nextAll("p:contains(6)");
        assertEquals(1, nextAF.size());
        assertEquals("6", nextAF.first().text());

        Elements prev = els.prev();
        assertEquals(2, prev.size());
        assertEquals("3", prev.first().text());
        assertEquals("9", prev.last().text());

        assertEquals(0, els.prev("p:contains(1)").size());
        final Elements prevF = els.prev("p:contains(3)");
        assertEquals(1, prevF.size());
        assertEquals("3", prevF.first().text());

        Elements prevA = els.prevAll();
        assertEquals(6, prevA.size());
        assertEquals("3", prevA.first().text());
        assertEquals("7", prevA.last().text());

        Elements prevAF = els.prevAll("p:contains(1)");
        assertEquals(1, prevAF.size());
        assertEquals("1", prevAF.first().text());
    }

// org.jsoup.select.ElementsTest::eachText
    @Test public void eachText() {
        Document doc = Jsoup.parse("<div><p>1<p>2<p>3<p>4<p>5<p>6</div><div><p>7<p>8<p>9<p>10<p>11<p>12<p></p></div>");
        List<String> divText = doc.select("div").eachText();
        assertEquals(2, divText.size());
        assertEquals("1 2 3 4 5 6", divText.get(0));
        assertEquals("7 8 9 10 11 12", divText.get(1));

        List<String> pText = doc.select("p").eachText();
        Elements ps = doc.select("p");
        assertEquals(13, ps.size());
        assertEquals(12, pText.size()); 
        assertEquals("1", pText.get(0));
        assertEquals("2", pText.get(1));
        assertEquals("5", pText.get(4));
        assertEquals("7", pText.get(6));
        assertEquals("12", pText.get(11));
    }

// org.jsoup.select.ElementsTest::eachAttr
    @Test public void eachAttr() {
        Document doc = Jsoup.parse(
            "<div><a href='/foo'>1</a><a href='http://example.com/bar'>2</a><a href=''>3</a><a>4</a>",
            "http://example.com");

        List<String> hrefAttrs = doc.select("a").eachAttr("href");
        assertEquals(3, hrefAttrs.size());
        assertEquals("/foo", hrefAttrs.get(0));
        assertEquals("http://example.com/bar", hrefAttrs.get(1));
        assertEquals("", hrefAttrs.get(2));
        assertEquals(4, doc.select("a").size());

        List<String> absAttrs = doc.select("a").eachAttr("abs:href");
        assertEquals(3, absAttrs.size());
        assertEquals(3, absAttrs.size());
        assertEquals("http://example.com/foo", absAttrs.get(0));
        assertEquals("http://example.com/bar", absAttrs.get(1));
        assertEquals("http://example.com", absAttrs.get(2));
    }

// org.jsoup.select.SelectorTest::testByTag
    @Test public void testByTag() {
        
        Elements els = Jsoup.parse("<div id=1><div id=2><p>Hello</p></div></div><DIV id=3>").select("DIV");
        assertEquals(3, els.size());
        assertEquals("1", els.get(0).id());
        assertEquals("2", els.get(1).id());
        assertEquals("3", els.get(2).id());

        Elements none = Jsoup.parse("<div id=1><div id=2><p>Hello</p></div></div><div id=3>").select("span");
        assertEquals(0, none.size());
    }

// org.jsoup.select.SelectorTest::testById
    @Test public void testById() {
        Elements els = Jsoup.parse("<div><p id=foo>Hello</p><p id=foo>Foo two!</p></div>").select("#foo");
        assertEquals(2, els.size());
        assertEquals("Hello", els.get(0).text());
        assertEquals("Foo two!", els.get(1).text());

        Elements none = Jsoup.parse("<div id=1></div>").select("#foo");
        assertEquals(0, none.size());
    }

// org.jsoup.select.SelectorTest::testByClass
    @Test public void testByClass() {
        Elements els = Jsoup.parse("<p id=0 class='ONE two'><p id=1 class='one'><p id=2 class='two'>").select("P.One");
        assertEquals(2, els.size());
        assertEquals("0", els.get(0).id());
        assertEquals("1", els.get(1).id());

        Elements none = Jsoup.parse("<div class='one'></div>").select(".foo");
        assertEquals(0, none.size());

        Elements els2 = Jsoup.parse("<div class='One-Two'></div>").select(".one-two");
        assertEquals(1, els2.size());
    }

// org.jsoup.select.SelectorTest::testByClassCaseInsensitive
    @Test public void testByClassCaseInsensitive() {
        String html = "<p Class=foo>One <p Class=Foo>Two <p class=FOO>Three <p class=farp>Four";
        Elements elsFromClass = Jsoup.parse(html).select("P.Foo");
        Elements elsFromAttr = Jsoup.parse(html).select("p[class=foo]");

        assertEquals(elsFromAttr.size(), elsFromClass.size());
        assertEquals(3, elsFromClass.size());
        assertEquals("Two", elsFromClass.get(1).text());
    }

// org.jsoup.select.SelectorTest::testNamespacedTag
    @Test public void testNamespacedTag() {
        Document doc = Jsoup.parse("<div><abc:def id=1>Hello</abc:def></div> <abc:def class=bold id=2>There</abc:def>");
        Elements byTag = doc.select("abc|def");
        assertEquals(2, byTag.size());
        assertEquals("1", byTag.first().id());
        assertEquals("2", byTag.last().id());

        Elements byAttr = doc.select(".bold");
        assertEquals(1, byAttr.size());
        assertEquals("2", byAttr.last().id());

        Elements byTagAttr = doc.select("abc|def.bold");
        assertEquals(1, byTagAttr.size());
        assertEquals("2", byTagAttr.last().id());

        Elements byContains = doc.select("abc|def:contains(e)");
        assertEquals(2, byContains.size());
        assertEquals("1", byContains.first().id());
        assertEquals("2", byContains.last().id());
    }

// org.jsoup.select.SelectorTest::testWildcardNamespacedTag
    @Test public void testWildcardNamespacedTag() {
        Document doc = Jsoup.parse("<div><abc:def id=1>Hello</abc:def></div> <abc:def class=bold id=2>There</abc:def>");
        Elements byTag = doc.select("*|def");
        assertEquals(2, byTag.size());
        assertEquals("1", byTag.first().id());
        assertEquals("2", byTag.last().id());

        Elements byAttr = doc.select(".bold");
        assertEquals(1, byAttr.size());
        assertEquals("2", byAttr.last().id());

        Elements byTagAttr = doc.select("*|def.bold");
        assertEquals(1, byTagAttr.size());
        assertEquals("2", byTagAttr.last().id());

        Elements byContains = doc.select("*|def:contains(e)");
        assertEquals(2, byContains.size());
        assertEquals("1", byContains.first().id());
        assertEquals("2", byContains.last().id());
    }

// org.jsoup.select.SelectorTest::testByAttributeRegex
    @Test public void testByAttributeRegex() {
        Document doc = Jsoup.parse("<p><img src=foo.png id=1><img src=bar.jpg id=2><img src=qux.JPEG id=3><img src=old.gif><img></p>");
        Elements imgs = doc.select("img[src~=(?i)\\.(png|jpe?g)]");
        assertEquals(3, imgs.size());
        assertEquals("1", imgs.get(0).id());
        assertEquals("2", imgs.get(1).id());
        assertEquals("3", imgs.get(2).id());
    }

// org.jsoup.select.SelectorTest::testByAttributeRegexCharacterClass
    @Test public void testByAttributeRegexCharacterClass() {
        Document doc = Jsoup.parse("<p><img src=foo.png id=1><img src=bar.jpg id=2><img src=qux.JPEG id=3><img src=old.gif id=4></p>");
        Elements imgs = doc.select("img[src~=[o]]");
        assertEquals(2, imgs.size());
        assertEquals("1", imgs.get(0).id());
        assertEquals("4", imgs.get(1).id());
    }

// org.jsoup.select.SelectorTest::testByAttributeRegexCombined
    @Test public void testByAttributeRegexCombined() {
        Document doc = Jsoup.parse("<div><table class=x><td>Hello</td></table></div>");
        Elements els = doc.select("div table[class~=x|y]");
        assertEquals(1, els.size());
        assertEquals("Hello", els.text());
    }

// org.jsoup.select.SelectorTest::testCombinedWithContains
    @Test public void testCombinedWithContains() {
        Document doc = Jsoup.parse("<p id=1>One</p><p>Two +</p><p>Three +</p>");
        Elements els = doc.select("p#1 + :contains(+)");
        assertEquals(1, els.size());
        assertEquals("Two +", els.text());
        assertEquals("p", els.first().tagName());
    }

// org.jsoup.select.SelectorTest::testAllElements
    @Test public void testAllElements() {
        String h = "<div><p>Hello</p><p><b>there</b></p></div>";
        Document doc = Jsoup.parse(h);
        Elements allDoc = doc.select("*");
        Elements allUnderDiv = doc.select("div *");
        assertEquals(8, allDoc.size());
        assertEquals(3, allUnderDiv.size());
        assertEquals("p", allUnderDiv.first().tagName());
    }

// org.jsoup.select.SelectorTest::testAllWithClass
    @Test public void testAllWithClass() {
        String h = "<p class=first>One<p class=first>Two<p>Three";
        Document doc = Jsoup.parse(h);
        Elements ps = doc.select("*.first");
        assertEquals(2, ps.size());
    }

// org.jsoup.select.SelectorTest::testGroupOr
    @Test public void testGroupOr() {
        String h = "<div title=foo /><div title=bar /><div /><p></p><img /><span title=qux>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select("p,div,[title]");

        assertEquals(5, els.size());
        assertEquals("div", els.get(0).tagName());
        assertEquals("foo", els.get(0).attr("title"));
        assertEquals("div", els.get(1).tagName());
        assertEquals("bar", els.get(1).attr("title"));
        assertEquals("div", els.get(2).tagName());
        assertTrue(els.get(2).attr("title").length() == 0); 
        assertFalse(els.get(2).hasAttr("title"));
        assertEquals("p", els.get(3).tagName());
        assertEquals("span", els.get(4).tagName());
    }

// org.jsoup.select.SelectorTest::testGroupOrAttribute
    @Test public void testGroupOrAttribute() {
        String h = "<div id=1 /><div id=2 /><div title=foo /><div title=bar />";
        Elements els = Jsoup.parse(h).select("[id],[title=foo]");

        assertEquals(3, els.size());
        assertEquals("1", els.get(0).id());
        assertEquals("2", els.get(1).id());
        assertEquals("foo", els.get(2).attr("title"));
    }

// org.jsoup.select.SelectorTest::descendant
    @Test public void descendant() {
        String h = "<div class=head><p class=first>Hello</p><p>There</p></div><p>None</p>";
        Document doc = Jsoup.parse(h);
        Element root = doc.getElementsByClass("HEAD").first();
        
        Elements els = root.select(".head p");
        assertEquals(2, els.size());
        assertEquals("Hello", els.get(0).text());
        assertEquals("There", els.get(1).text());

        Elements p = root.select("p.first");
        assertEquals(1, p.size());
        assertEquals("Hello", p.get(0).text());

        Elements empty = root.select("p .first"); 
        assertEquals(0, empty.size());
        
        Elements aboveRoot = root.select("body div.head");
        assertEquals(0, aboveRoot.size());
    }

// org.jsoup.select.SelectorTest::and
    @Test public void and() {
        String h = "<div id=1 class='foo bar' title=bar name=qux><p class=foo title=bar>Hello</p></div";
        Document doc = Jsoup.parse(h);

        Elements div = doc.select("div.foo");
        assertEquals(1, div.size());
        assertEquals("div", div.first().tagName());

        Elements p = doc.select("div .foo"); 
        assertEquals(1, p.size());
        assertEquals("p", p.first().tagName());

        Elements div2 = doc.select("div#1.foo.bar[title=bar][name=qux]"); 
        assertEquals(1, div2.size());
        assertEquals("div", div2.first().tagName());

        Elements p2 = doc.select("div *.foo"); 
        assertEquals(1, p2.size());
        assertEquals("p", p2.first().tagName());
    }

// org.jsoup.select.SelectorTest::deeperDescendant
    @Test public void deeperDescendant() {
        String h = "<div class=head><p><span class=first>Hello</div><div class=head><p class=first><span>Another</span><p>Again</div>";
        Document doc = Jsoup.parse(h);
        Element root = doc.getElementsByClass("head").first();

        Elements els = root.select("div p .first");
        assertEquals(1, els.size());
        assertEquals("Hello", els.first().text());
        assertEquals("span", els.first().tagName());

        Elements aboveRoot = root.select("body p .first");
        assertEquals(0, aboveRoot.size());
    }

// org.jsoup.select.SelectorTest::parentChildElement
    @Test public void parentChildElement() {
        String h = "<div id=1><div id=2><div id = 3></div></div></div><div id=4></div>";
        Document doc = Jsoup.parse(h);

        Elements divs = doc.select("div > div");
        assertEquals(2, divs.size());
        assertEquals("2", divs.get(0).id()); 
        assertEquals("3", divs.get(1).id()); 

        Elements div2 = doc.select("div#1 > div");
        assertEquals(1, div2.size());
        assertEquals("2", div2.get(0).id());
    }

// org.jsoup.select.SelectorTest::parentWithClassChild
    @Test public void parentWithClassChild() {
        String h = "<h1 class=foo><a href=1 /></h1><h1 class=foo><a href=2 class=bar /></h1><h1><a href=3 /></h1>";
        Document doc = Jsoup.parse(h);

        Elements allAs = doc.select("h1 > a");
        assertEquals(3, allAs.size());
        assertEquals("a", allAs.first().tagName());

        Elements fooAs = doc.select("h1.foo > a");
        assertEquals(2, fooAs.size());
        assertEquals("a", fooAs.first().tagName());

        Elements barAs = doc.select("h1.foo > a.bar");
        assertEquals(1, barAs.size());
    }

// org.jsoup.select.SelectorTest::parentChildStar
    @Test public void parentChildStar() {
        String h = "<div id=1><p>Hello<p><b>there</b></p></div><div id=2><span>Hi</span></div>";
        Document doc = Jsoup.parse(h);
        Elements divChilds = doc.select("div > *");
        assertEquals(3, divChilds.size());
        assertEquals("p", divChilds.get(0).tagName());
        assertEquals("p", divChilds.get(1).tagName());
        assertEquals("span", divChilds.get(2).tagName());
    }

// org.jsoup.select.SelectorTest::multiChildDescent
    @Test public void multiChildDescent() {
        String h = "<div id=foo><h1 class=bar><a href=http://example.com/>One</a></h1></div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select("div#foo > h1.bar > a[href*=example]");
        assertEquals(1, els.size());
        assertEquals("a", els.first().tagName());
    }

// org.jsoup.select.SelectorTest::caseInsensitive
    @Test public void caseInsensitive() {
        String h = "<dIv tItle=bAr><div>"; 
        Document doc = Jsoup.parse(h);

        assertEquals(2, doc.select("DiV").size());
        assertEquals(1, doc.select("DiV[TiTLE]").size());
        assertEquals(1, doc.select("DiV[TiTLE=BAR]").size());
        assertEquals(0, doc.select("DiV[TiTLE=BARBARELLA]").size());
    }

// org.jsoup.select.SelectorTest::adjacentSiblings
    @Test public void adjacentSiblings() {
        String h = "<ol><li>One<li>Two<li>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("li + li");
        assertEquals(2, sibs.size());
        assertEquals("Two", sibs.get(0).text());
        assertEquals("Three", sibs.get(1).text());
    }

// org.jsoup.select.SelectorTest::adjacentSiblingsWithId
    @Test public void adjacentSiblingsWithId() {
        String h = "<ol><li id=1>One<li id=2>Two<li id=3>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("li#1 + li#2");
        assertEquals(1, sibs.size());
        assertEquals("Two", sibs.get(0).text());
    }

// org.jsoup.select.SelectorTest::notAdjacent
    @Test public void notAdjacent() {
        String h = "<ol><li id=1>One<li id=2>Two<li id=3>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("li#1 + li#3");
        assertEquals(0, sibs.size());
    }

// org.jsoup.select.SelectorTest::mixCombinator
    @Test public void mixCombinator() {
        String h = "<div class=foo><ol><li>One<li>Two<li>Three</ol></div>";
        Document doc = Jsoup.parse(h);
        Elements sibs = doc.select("body > div.foo li + li");

        assertEquals(2, sibs.size());
        assertEquals("Two", sibs.get(0).text());
        assertEquals("Three", sibs.get(1).text());
    }

// org.jsoup.select.SelectorTest::mixCombinatorGroup
    @Test public void mixCombinatorGroup() {
        String h = "<div class=foo><ol><li>One<li>Two<li>Three</ol></div>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select(".foo > ol, ol > li + li");

        assertEquals(3, els.size());
        assertEquals("ol", els.get(0).tagName());
        assertEquals("Two", els.get(1).text());
        assertEquals("Three", els.get(2).text());
    }

// org.jsoup.select.SelectorTest::generalSiblings
    @Test public void generalSiblings() {
        String h = "<ol><li id=1>One<li id=2>Two<li id=3>Three</ol>";
        Document doc = Jsoup.parse(h);
        Elements els = doc.select("#1 ~ #3");
        assertEquals(1, els.size());
        assertEquals("Three", els.first().text());
    }

// org.jsoup.select.SelectorTest::testCharactersInIdAndClass
    @Test public void testCharactersInIdAndClass() {
        
        String h = "<div><p id='a1-foo_bar'>One</p><p class='b2-qux_bif'>Two</p></div>";
        Document doc = Jsoup.parse(h);

        Element el1 = doc.getElementById("a1-foo_bar");
        assertEquals("One", el1.text());
        Element el2 = doc.getElementsByClass("b2-qux_bif").first();
        assertEquals("Two", el2.text());

        Element el3 = doc.select("#a1-foo_bar").first();
        assertEquals("One", el3.text());
        Element el4 = doc.select(".b2-qux_bif").first();
        assertEquals("Two", el4.text());
    }

// org.jsoup.select.SelectorTest::testSupportsLeadingCombinator
    @Test public void testSupportsLeadingCombinator() {
        String h = "<div><p><span>One</span><span>Two</span></p></div>";
        Document doc = Jsoup.parse(h);

        Element p = doc.select("div > p").first();
        Elements spans = p.select("> span");
        assertEquals(2, spans.size());
        assertEquals("One", spans.first().text());

        
        h = "<div id=1><div id=2><div id=3></div></div></div>";
        doc = Jsoup.parse(h);
        Element div = doc.select("div").select(" > div").first();
        assertEquals("2", div.id());
    }

// org.jsoup.select.SelectorTest::testPseudoLessThan
    @Test public void testPseudoLessThan() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</>p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:lt(2)");
        assertEquals(3, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Two", ps.get(1).text());
        assertEquals("Four", ps.get(2).text());
    }

// org.jsoup.select.SelectorTest::testPseudoGreaterThan
    @Test public void testPseudoGreaterThan() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:gt(0)");
        assertEquals(2, ps.size());
        assertEquals("Two", ps.get(0).text());
        assertEquals("Three", ps.get(1).text());
    }

// org.jsoup.select.SelectorTest::testPseudoEquals
    @Test public void testPseudoEquals() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</>p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:eq(0)");
        assertEquals(2, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Four", ps.get(1).text());

        Elements ps2 = doc.select("div:eq(0) p:eq(0)");
        assertEquals(1, ps2.size());
        assertEquals("One", ps2.get(0).text());
        assertEquals("p", ps2.get(0).tagName());
    }

// org.jsoup.select.SelectorTest::testPseudoBetween
    @Test public void testPseudoBetween() {
        Document doc = Jsoup.parse("<div><p>One</p><p>Two</p><p>Three</>p></div><div><p>Four</p>");
        Elements ps = doc.select("div p:gt(0):lt(2)");
        assertEquals(1, ps.size());
        assertEquals("Two", ps.get(0).text());
    }

// org.jsoup.select.SelectorTest::testPseudoCombined
    @Test public void testPseudoCombined() {
        Document doc = Jsoup.parse("<div class='foo'><p>One</p><p>Two</p></div><div><p>Three</p><p>Four</p></div>");
        Elements ps = doc.select("div.foo p:gt(0)");
        assertEquals(1, ps.size());
        assertEquals("Two", ps.get(0).text());
    }

// org.jsoup.select.SelectorTest::testPseudoHas
    @Test public void testPseudoHas() {
        Document doc = Jsoup.parse("<div id=0><p><span>Hello</span></p></div> <div id=1><span class=foo>There</span></div> <div id=2><p>Not</p></div>");

        Elements divs1 = doc.select("div:has(span)");
        assertEquals(2, divs1.size());
        assertEquals("0", divs1.get(0).id());
        assertEquals("1", divs1.get(1).id());

        Elements divs2 = doc.select("div:has([class])");
        assertEquals(1, divs2.size());
        assertEquals("1", divs2.get(0).id());

        Elements divs3 = doc.select("div:has(span, p)");
        assertEquals(3, divs3.size());
        assertEquals("0", divs3.get(0).id());
        assertEquals("1", divs3.get(1).id());
        assertEquals("2", divs3.get(2).id());

        Elements els1 = doc.body().select(":has(p)");
        assertEquals(3, els1.size()); 
        assertEquals("body", els1.first().tagName());
        assertEquals("0", els1.get(1).id());
        assertEquals("2", els1.get(2).id());
    }

// org.jsoup.select.SelectorTest::testNestedHas
    @Test public void testNestedHas() {
        Document doc = Jsoup.parse("<div><p><span>One</span></p></div> <div><p>Two</p></div>");
        Elements divs = doc.select("div:has(p:has(span))");
        assertEquals(1, divs.size());
        assertEquals("One", divs.first().text());

        
        divs = doc.select("div:has(p:matches((?i)two))");
        assertEquals(1, divs.size());
        assertEquals("div", divs.first().tagName());
        assertEquals("Two", divs.first().text());

        
        divs = doc.select("div:has(p:contains(two))");
        assertEquals(1, divs.size());
        assertEquals("div", divs.first().tagName());
        assertEquals("Two", divs.first().text());
    }

// org.jsoup.select.SelectorTest::testPsuedoContainsWithParentheses
    @Test public void testPsuedoContainsWithParentheses() {
        Document doc = Jsoup.parse("<div><p id=1>This (is good)</p><p id=2>This is bad)</p>");

        Elements ps1 = doc.select("p:contains(this (is good))");
        assertEquals(1, ps1.size());
        assertEquals("1", ps1.first().id());

        Elements ps2 = doc.select("p:contains(this is bad\\))");
        assertEquals(1, ps2.size());
        assertEquals("2", ps2.first().id());
    }

// org.jsoup.select.SelectorTest::testMatches
    @Test public void testMatches() {
        Document doc = Jsoup.parse("<p id=1>The <i>Rain</i></p> <p id=2>There are 99 bottles.</p> <p id=3>Harder (this)</p> <p id=4>Rain</p>");

        Elements p1 = doc.select("p:matches(The rain)"); 
        assertEquals(0, p1.size());

        Elements p2 = doc.select("p:matches((?i)the rain)"); 
        assertEquals(1, p2.size());
        assertEquals("1", p2.first().id());

        Elements p4 = doc.select("p:matches((?i)^rain$)"); 
        assertEquals(1, p4.size());
        assertEquals("4", p4.first().id());

        Elements p5 = doc.select("p:matches(\\d+)");
        assertEquals(1, p5.size());
        assertEquals("2", p5.first().id());

        Elements p6 = doc.select("p:matches(\\w+\\s+\\(\\w+\\))"); 
        assertEquals(1, p6.size());
        assertEquals("3", p6.first().id());

        Elements p7 = doc.select("p:matches((?i)the):has(i)"); 
        assertEquals(1, p7.size());
        assertEquals("1", p7.first().id());
    }

// org.jsoup.select.SelectorTest::matchesOwn
    @Test public void matchesOwn() {
        Document doc = Jsoup.parse("<p id=1>Hello <b>there</b> now</p>");

        Elements p1 = doc.select("p:matchesOwn((?i)hello now)");
        assertEquals(1, p1.size());
        assertEquals("1", p1.first().id());

        assertEquals(0, doc.select("p:matchesOwn(there)").size());
    }

// org.jsoup.select.SelectorTest::testRelaxedTags
    @Test public void testRelaxedTags() {
        Document doc = Jsoup.parse("<abc_def id=1>Hello</abc_def> <abc-def id=2>There</abc-def>");

        Elements el1 = doc.select("abc_def");
        assertEquals(1, el1.size());
        assertEquals("1", el1.first().id());

        Elements el2 = doc.select("abc-def");
        assertEquals(1, el2.size());
        assertEquals("2", el2.first().id());
    }

// org.jsoup.select.SelectorTest::notParas
    @Test public void notParas() {
        Document doc = Jsoup.parse("<p id=1>One</p> <p>Two</p> <p><span>Three</span></p>");

        Elements el1 = doc.select("p:not([id=1])");
        assertEquals(2, el1.size());
        assertEquals("Two", el1.first().text());
        assertEquals("Three", el1.last().text());

        Elements el2 = doc.select("p:not(:has(span))");
        assertEquals(2, el2.size());
        assertEquals("One", el2.first().text());
        assertEquals("Two", el2.last().text());
    }

// org.jsoup.select.SelectorTest::notAll
    @Test public void notAll() {
        Document doc = Jsoup.parse("<p>Two</p> <p><span>Three</span></p>");

        Elements el1 = doc.body().select(":not(p)"); 
        assertEquals(2, el1.size());
        assertEquals("body", el1.first().tagName());
        assertEquals("span", el1.last().tagName());
    }

// org.jsoup.select.SelectorTest::notClass
    @Test public void notClass() {
        Document doc = Jsoup.parse("<div class=left>One</div><div class=right id=1><p>Two</p></div>");

        Elements el1 = doc.select("div:not(.left)");
        assertEquals(1, el1.size());
        assertEquals("1", el1.first().id());
    }

// org.jsoup.select.SelectorTest::handlesCommasInSelector
    @Test public void handlesCommasInSelector() {
        Document doc = Jsoup.parse("<p name='1,2'>One</p><div>Two</div><ol><li>123</li><li>Text</li></ol>");

        Elements ps = doc.select("[name=1,2]");
        assertEquals(1, ps.size());

        Elements containers = doc.select("div, li:matches([0-9,]+)");
        assertEquals(2, containers.size());
        assertEquals("div", containers.get(0).tagName());
        assertEquals("li", containers.get(1).tagName());
        assertEquals("123", containers.get(1).text());
    }

// org.jsoup.select.SelectorTest::selectSupplementaryCharacter
    @Test public void selectSupplementaryCharacter() {
        String s = new String(Character.toChars(135361));
        Document doc = Jsoup.parse("<div k" + s + "='" + s + "'>^" + s +"$/div>");
        assertEquals("div", doc.select("div[k" + s + "]").first().tagName());
        assertEquals("div", doc.select("div:containsOwn(" + s + ")").first().tagName());
    }

// org.jsoup.select.SelectorTest::selectClassWithSpace
    public void selectClassWithSpace() {
        final String html = "<div class=\"value\">class without space</div>\n"
                          + "<div class=\"value \">class with space</div>";
        
        Document doc = Jsoup.parse(html);
        
        Elements found = doc.select("div[class=value ]");
        assertEquals(2, found.size());
        assertEquals("class without space", found.get(0).text());
        assertEquals("class with space", found.get(1).text());
        
        found = doc.select("div[class=\"value \"]");
        assertEquals(2, found.size());
        assertEquals("class without space", found.get(0).text());
        assertEquals("class with space", found.get(1).text());
        
        found = doc.select("div[class=\"value\\ \"]");
        assertEquals(0, found.size());
    }

// org.jsoup.select.SelectorTest::selectSameElements
    @Test public void selectSameElements() {
        final String html = "<div>one</div><div>one</div>";

        Document doc = Jsoup.parse(html);
        Elements els = doc.select("div");
        assertEquals(2, els.size());

        Elements subSelect = els.select(":contains(one)");
        assertEquals(2, subSelect.size());
    }

// org.jsoup.select.SelectorTest::attributeWithBrackets
    @Test public void attributeWithBrackets() {
        String html = "<div data='End]'>One</div> <div data='[Another)]]'>Two</div>";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.select("div[data='End]']").first().text());
        assertEquals("Two", doc.select("div[data='[Another)]]']").first().text());
        assertEquals("One", doc.select("div[data=\"End]\"]").first().text());
        assertEquals("Two", doc.select("div[data=\"[Another)]]\"]").first().text());
    }

// org.jsoup.select.SelectorTest::containsWithQuote
    @Test public void containsWithQuote() {
        String html = "<p>One'One</p><p>One'Two</p>";
        Document doc = Jsoup.parse(html);
        Elements els = doc.select("p:contains(One\\'One)");
        assertEquals(1, els.size());
        assertEquals("One'One", els.text());
    }

// org.jsoup.select.SelectorTest::selectFirst
    @Test public void selectFirst() {
        String html = "<p>One<p>Two<p>Three";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.selectFirst("p").text());
    }

// org.jsoup.select.SelectorTest::selectFirstWithAnd
    @Test public void selectFirstWithAnd() {
        String html = "<p>One<p class=foo>Two<p>Three";
        Document doc = Jsoup.parse(html);
        assertEquals("Two", doc.selectFirst("p.foo").text());
    }

// org.jsoup.select.SelectorTest::selectFirstWithOr
    @Test public void selectFirstWithOr() {
        String html = "<p>One<p>Two<p>Three<div>Four";
        Document doc = Jsoup.parse(html);
        assertEquals("One", doc.selectFirst("p, div").text());
    }

// org.jsoup.select.TraversorTest::filterVisit
    public void filterVisit() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterSkipChildren
    public void filterSkipChildren() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.SKIP_CHILDREN : FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p></p></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterSkipEntirely
    public void filterSkipEntirely() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                
                if ("p".equals(node.nodeName()))
                    return FilterResult.SKIP_ENTIRELY;
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterRemove
    public void filterRemove() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There be <b>bold</b></div>");
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                
                return ("p".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                
                return ("b".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div>\n<div>\n There be \n</div>", doc.select("body").html());
    }

// org.jsoup.select.TraversorTest::filterStop
    public void filterStop() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            public FilterResult head(Node node, int depth) {
                accum.append("<" + node.nodeName() + ">");
                return FilterResult.CONTINUE;
            }

            public FilterResult tail(Node node, int depth) {
                accum.append("</" + node.nodeName() + ">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.STOP : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p>", accum.toString());
    }
