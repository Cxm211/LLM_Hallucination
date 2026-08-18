// org/jsoup/nodes/FormElementTest.java
@Test public void skipsDisabledRadioButton() {
    String html = "<form><input type='radio' name='option' value='yes' checked disabled><input type='radio' name='option' value='no'></form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.select("form").first();
    List<Connection.KeyVal> data = form.formData();
    assertEquals(0, data.size());
}