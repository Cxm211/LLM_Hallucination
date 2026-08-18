// org/jsoup/nodes/FormElementTest.java
@Test public void includesEnabledTextareaWithDisabledInput() {
    String html = "<form><textarea name='comment'>Hello</textarea><input name='disabled_field' value='value' disabled></form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.select("form").first();
    List<Connection.KeyVal> data = form.formData();
    assertEquals(1, data.size());
    assertEquals("comment=Hello", data.get(0).toString());
}