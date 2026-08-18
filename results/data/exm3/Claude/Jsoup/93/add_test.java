// org/jsoup/nodes/FormElementTest.java
@Test public void excludesSubmitAndResetTypes() {
    String html = "<form><input name='one' type='submit' value='Submit'><input name='two' type='reset' value='Reset'><input name='three' type='image' value='Image'><input name='four' type='text' value='included'></form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.select("form").first();
    List<Connection.KeyVal> data = form.formData();

    assertEquals(1, data.size());
    assertEquals("four=included", data.get(0).toString());
}