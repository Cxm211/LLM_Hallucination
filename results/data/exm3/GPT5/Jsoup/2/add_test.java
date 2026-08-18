// org/jsoup/parser/ParserTest.java::handlesTextAfterData_Title
@Test public void handlesTextAfterData_Title() {
        String h = "<html><body>pre <title>inner</title> aft</body></html>";
        Document doc = Jsoup.parse(h);
        assertEquals("<html><head><title>inner</title></head><body>pre  aft</body></html>", TextUtil.stripNewlines(doc.html()));
    }