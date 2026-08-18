// buggy function
    CharacterReader(String input) {
        Validate.notNull(input);

        this.input = input;
        this.length = input.length();
    }

    String consumeToEnd() {
        String data = input.substring(pos, input.length() - 1);
        pos = input.length();
        return data;
    }

// trigger testcase
// org/jsoup/parser/CharacterReaderTest.java::consumeToEnd
@Test public void consumeToEnd() {
        String in = "one two three";
        CharacterReader r = new CharacterReader(in);
        String toEnd = r.consumeToEnd();
        assertEquals(in, toEnd);
        assertTrue(r.isEmpty());
    }

// org/jsoup/parser/CharacterReaderTest.java::handleCarriageReturnAsLineFeed
@Test public void handleCarriageReturnAsLineFeed() {
        String in = "one \r two \r\n three";
        CharacterReader r = new CharacterReader(in);

        String first = r.consumeTo('\n');
        assertEquals("one ", first);
        assertEquals("\n two \n three", r.consumeToEnd());
    }

// org/jsoup/parser/ParserTest.java::handlesNewlinesAndWhitespaceInTag
@Test public void handlesNewlinesAndWhitespaceInTag() {
        Document doc = Jsoup.parse("<a \n href=\"one\" \r\n id=\"two\" \f >");
        assertEquals("<a href=\"one\" id=\"two\"></a>", doc.body().html());
    }
