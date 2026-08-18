// org/jsoup/nodes/FormElementTest.java
@Test public void multipleSelectWithNoSelectedOptions() {
        String html = "<form><select name='foo' multiple><option value='a'>A</option><option value='b'>B</option></select></form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertEquals(0, data.size());
    }
