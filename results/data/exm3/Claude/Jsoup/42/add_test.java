// org/jsoup/nodes/FormElementTest.java
@Test public void skipsDisabledSelectElement() {
    String html = "<form><select name='choice' disabled><option value='one'>One</option><option value='two' selected>Two</option></select></form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.select("form").first();
    List<Connection.KeyVal> data = form.formData();
    assertEquals(0, data.size());
}