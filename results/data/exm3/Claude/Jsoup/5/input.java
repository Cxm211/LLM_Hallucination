// buggy function
    private Attribute parseAttribute() {
        tq.consumeWhitespace();
        String key = tq.consumeAttributeKey();
        String value = "";
        tq.consumeWhitespace();
        if (tq.matchChomp("=")) {
            tq.consumeWhitespace();

            if (tq.matchChomp(SQ)) {
                value = tq.chompTo(SQ);
            } else if (tq.matchChomp(DQ)) {
                value = tq.chompTo(DQ);
            } else {
                StringBuilder valueAccum = new StringBuilder();
                // no ' or " to look for, so scan to end tag or space (or end of stream)
                while (!tq.matchesAny("<", "/>", ">") && !tq.matchesWhitespace() && !tq.isEmpty()) {
                    valueAccum.append(tq.consume());
                }
                value = valueAccum.toString();
            }
            tq.consumeWhitespace();
        }
        if (key.length() != 0)
            return Attribute.createFromEncoded(key, value);
        else {
            tq.consume();
                
            return null;
        }
    }

// trigger testcase
// org/jsoup/parser/ParserTest.java::parsesQuiteRoughAttributes
@Test public void parsesQuiteRoughAttributes() {
        String html = "<p =a>One<a =a";
        Document doc = Jsoup.parse(html);
        assertEquals("<p>One<a></a></p>", doc.body().html());
        
        doc = Jsoup.parse("<p .....");
        assertEquals("<p></p>", doc.body().html());
        
        doc = Jsoup.parse("<p .....<p!!");
        assertEquals("<p></p>\n<p></p>", doc.body().html());
    }
