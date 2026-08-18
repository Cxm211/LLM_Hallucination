// org/jsoup/nodes/FormElementTest.java
@Test public void handlesRadioWithoutChecked() {
    String html = "<form><input name='radio1' type='radio' value='a'><input name='radio1' type='radio' value='b'><input name='test' value='included'></form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.select("form").first();
    List<Connection.KeyVal> data = form.formData();

    assertEquals(1, data.size());
    assertEquals("test=included", data.get(0).toString());
}