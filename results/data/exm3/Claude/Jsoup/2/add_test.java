// org/jsoup/parser/ParserTest.java
@Test public void handlesMultipleDataTagsInSequence() {
        String h = "<html><body><script>first</script><script>second</script></body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body><script>first</script><script>second</script></body></html>", TextUtil.stripNewlines(doc.html()));
    }