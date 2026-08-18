// org/jsoup/parser/ParserTest.java
@Test public void handlesDataTagWithTextBetween() {
        String h = "<html><body><script>js1</script>text<script>js2</script>more</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head></head><body><script>js1</script>text<script>js2</script>more</body></html>", TextUtil.stripNewlines(doc.html()));
    }