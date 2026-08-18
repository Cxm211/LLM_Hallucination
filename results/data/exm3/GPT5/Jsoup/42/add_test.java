// org/jsoup/nodes/FormElementTest.java::ignoresDisabledSelect
@Test public void ignoresDisabledSelect() {
        Document doc = Jsoup.parse("<form><select name='sel' disabled><option value='a' selected>Text</option></select></form>");
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();
        assertTrue(data.isEmpty());
    }