// org/jsoup/nodes/FormElementTest.java
@Test public void handlesEmptySelectWithoutOptions() {
    String html = "<form><select name='empty'></select><input name='test' value='val'></form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.select("form").first();
    List<Connection.KeyVal> data = form.formData();

    assertEquals(1, data.size());
    assertEquals("test=val", data.get(0).toString());
}