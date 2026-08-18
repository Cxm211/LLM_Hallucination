// org/jsoup/nodes/FormElementTest.java
@Test public void formDataWithEmptyValue() {
    String html = "<form><input name='check' type='checkbox' value='' checked>" +
                  "<input name='radio' type='radio' value='' checked></form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.select("form").first();
    List<Connection.KeyVal> data = form.formData();
    assertEquals(2, data.size());
    assertEquals("check=", data.get(0).toString());
    assertEquals("radio=", data.get(1).toString());
}
