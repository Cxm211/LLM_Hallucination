// org/jsoup/nodes/FormElementTest.java
@Test public void radioWithoutValueSubmitsOn() {
        String html = "<form><input type='radio' name='foo' checked></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(1, data.size());
        assertEquals("foo", data.get(0).key());
        assertEquals("on", data.get(0).value());
    }
