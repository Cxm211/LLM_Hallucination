// buggy code
    private void add(String key, String value) {
        checkCapacity(size + 1);
        keys[size] = key;
        vals[size] = value;
        size++;
    }

    public int size() {
        return size;
    }

    public void normalize() {
        for (int i = 0; i < size; i++) {
            keys[i] = lowerCase(keys[i]);
        }
    }

    Element insert(final Token.StartTag startTag) {
        // cleanup duplicate attributes:

        // handle empty unknown tags
        // when the spec expects an empty tag, will directly hit insertEmpty, so won't generate this fake end tag.
        if (startTag.isSelfClosing()) {
            Element el = insertEmpty(startTag);
            stack.add(el);
            tokeniser.transition(TokeniserState.Data); // handles <script />, otherwise needs breakout steps from script data
            tokeniser.emit(emptyEnd.reset().name(el.tagName()));  // ensure we get out of whatever state we are in. emitted for yielded processing
            return el;
        }

        Element el = new Element(Tag.valueOf(startTag.name(), settings), baseUri, settings.normalizeAttributes(startTag.attributes));
        insert(el);
        return el;
    }

    public boolean preserveTagCase() {
        return preserveTagCase;
    }

        final void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                pendingAttributeName = pendingAttributeName.trim();
                if (pendingAttributeName.length() > 0) {
                    String value;
                    if (hasPendingAttributeValue)
                        value = pendingAttributeValue.length() > 0 ? pendingAttributeValue.toString() : pendingAttributeValueS;
                    else if (hasEmptyAttributeValue)
                        value = "";
                    else
                        value = null;
                    // note that we add, not put. So that the first is kept, and rest are deduped, once in a context where case sensitivity is known (the appropriate tree builder).
                    attributes.put(pendingAttributeName, value);
                }
            }
            pendingAttributeName = null;
            hasEmptyAttributeValue = false;
            hasPendingAttributeValue = false;
            reset(pendingAttributeValue);
            pendingAttributeValueS = null;
        }

    Element insert(Token.StartTag startTag) {
        Tag tag = Tag.valueOf(startTag.name(), settings);
        // todo: wonder if for xml parsing, should treat all tags as unknown? because it's not html.

        Element el = new Element(tag, baseUri, settings.normalizeAttributes(startTag.attributes));
        insertNode(el);
        if (startTag.isSelfClosing()) {
            if (!tag.isKnownTag()) // unknown tag, remember this is self closing for output. see above.
                tag.setSelfClosing();
        } else {
            stack.add(el);
        }
        return el;
    }

// relevant test
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

        Elements els2 = doc.body().select(":has(> span)");
        assertEquals(2,els2.size()); 
        assertEquals("p",els2.first().tagName());
        assertEquals("1", els2.get(1).id());
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

// org.jsoup.select.SelectorTest::matchText
    @Test public void matchText() {
        String html = "<p>One<br>Two</p>";
        Document doc = Jsoup.parse(html);
        String origHtml = doc.html();

        Elements one = doc.select("p:matchText:first-child");
        assertEquals("One", one.first().text());

        Elements two = doc.select("p:matchText:last-child");
        assertEquals("Two", two.first().text());

        assertEquals(origHtml, doc.html());

        assertEquals("Two", doc.select("p:matchText + br + *").text());
    }

// org.jsoup.select.SelectorTest::splitOnBr
    @Test public void splitOnBr() {
        String html = "<div><p>One<br>Two<br>Three</p></div>";
        Document doc = Jsoup.parse(html);

        Elements els = doc.select("p:matchText");
        assertEquals(3, els.size());
        assertEquals("One", els.get(0).text());
        assertEquals("Two", els.get(1).text());
        assertEquals("Three", els.get(2).toString());
    }

// org.jsoup.select.SelectorTest::matchTextAttributes
    @Test public void matchTextAttributes() {
        Document doc = Jsoup.parse("<div><p class=one>One<br>Two<p class=two>Three<br>Four");
        Elements els = doc.select("p.two:matchText:last-child");

        assertEquals(1, els.size());
        assertEquals("Four", els.text());
    }

// org.jsoup.select.SelectorTest::findBetweenSpan
    @Test public void findBetweenSpan() {
        Document doc = Jsoup.parse("<p><span>One</span> Two <span>Three</span>");
        Elements els = doc.select("span ~ p:matchText"); 

        assertEquals(1, els.size());
        assertEquals("Two", els.text());
    }

// org.jsoup.select.TraversorTest::filterVisit
    public void filterVisit() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
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
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.SKIP_CHILDREN : FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
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
            @Override
            public FilterResult head(Node node, int depth) {
                
                if ("p".equals(node.nodeName()))
                    return FilterResult.SKIP_ENTIRELY;
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div><div><#text></#text></div>", accum.toString());
    }

// org.jsoup.select.TraversorTest::filterRemove
    public void filterRemove() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There be <b>bold</b></div>");
        NodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                
                return ("p".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }

            @Override
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
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                
                return ("p".equals(node.nodeName())) ? FilterResult.STOP : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p>", accum.toString());
    }
