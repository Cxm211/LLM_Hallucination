// org/jsoup/nodes/FormElementTest.java::excludesSubmitAndReset
@Test public void excludesSubmitAndReset() {
        String html = "<form><input name='a' value='1'><input name='s' type='submit' value='Send'><input name='r' type='reset' value='Reset'></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        assertEquals(1, data.size());
        assertEquals("a=1", data.get(0).toString());
    }