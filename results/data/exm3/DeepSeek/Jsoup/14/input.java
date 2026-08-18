// buggy function
    boolean isAppropriateEndTagToken() {
        return tagPending.tagName.equals(lastStartTag.tagName);
    }

        void read(Tokeniser t, CharacterReader r) {
            if (r.matches('/')) {
                t.createTempBuffer();
                t.advanceTransition(RCDATAEndTagOpen);
                // diverge from spec: got a start tag, but there's no appropriate end tag (</title>), so rather than
                // consuming to EOF; break out here
            } else {
                t.emit("<");
                t.transition(Rcdata);
            }
        }

// trigger testcase
// org/jsoup/parser/ParserTest.java::handlesUnclosedTitle
@Test public void handlesUnclosedTitle() {
        Document one = Jsoup.parse("<title>One <b>Two <b>Three</TITLE><p>Test</p>"); // has title, so <b> is plain text
        assertEquals("One <b>Two <b>Three", one.title());
        assertEquals("Test", one.select("p").first().text());

        Document two = Jsoup.parse("<title>One<b>Two <p>Test</p>"); // no title, so <b> causes </title> breakout
        assertEquals("One", two.title());
        assertEquals("<b>Two <p>Test</p></b>", two.body().html());
    }

// org/jsoup/parser/ParserTest.java::parsesUnterminatedTextarea
@Test public void parsesUnterminatedTextarea() {
        // don't parse right to end, but break on <p>
        Document doc = Jsoup.parse("<body><p><textarea>one<p>two");
        Element t = doc.select("textarea").first();
        assertEquals("one", t.text());
        assertEquals("two", doc.select("p").get(1).text());
    }
