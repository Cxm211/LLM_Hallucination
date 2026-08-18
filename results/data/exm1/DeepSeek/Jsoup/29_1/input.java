// buggy code
    public String title() {
        // title is a preserve whitespace tag (for document output), but normalised here
        Element titleEl = getElementsByTag("title").first();
        return titleEl != null ? titleEl.text().trim() : "";
    }

// relevant test
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

        Elements divs2 = doc.select("div:has([class]");
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

// org.jsoup.select.SelectorTest::testPseudoContains
    @Test public void testPseudoContains() {
        Document doc = Jsoup.parse("<div><p>The Rain.</p> <p class=light>The <i>rain</i>.</p> <p>Rain, the.</p></div>");

        Elements ps1 = doc.select("p:contains(Rain)");
        assertEquals(3, ps1.size());

        Elements ps2 = doc.select("p:contains(the rain)");
        assertEquals(2, ps2.size());
        assertEquals("The Rain.", ps2.first().html());
        assertEquals("The <i>rain</i>.", ps2.last().html());

        Elements ps3 = doc.select("p:contains(the Rain):has(i)");
        assertEquals(1, ps3.size());
        assertEquals("light", ps3.first().className());

        Elements ps4 = doc.select(".light:contains(rain)");
        assertEquals(1, ps4.size());
        assertEquals("light", ps3.first().className());

        Elements ps5 = doc.select(":contains(rain)");
        assertEquals(8, ps5.size()); 
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

// org.jsoup.select.SelectorTest::containsOwn
    @Test public void containsOwn() {
        Document doc = Jsoup.parse("<p id=1>Hello <b>there</b> now</p>");
        Elements ps = doc.select("p:containsOwn(Hello now)");
        assertEquals(1, ps.size());
        assertEquals("1", ps.first().id());

        assertEquals(0, doc.select("p:containsOwn(there)").size());
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
