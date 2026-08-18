    boolean matchesLetter() {
        if (isEmpty())
            return false;
        char c = input[pos];
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

// trigger testcase
@Test public void testSupportsNonAsciiTags() {
        String body = "<進捗推移グラフ>Yes</進捗推移グラフ>";
        Document doc = Jsoup.parse(body);
        Elements els = doc.select("進捗推移グラフ");
        assertEquals("Yes", els.text());
    }
