// org/jsoup/nodes/FormElementTest.java
@Test public void removeSelectFormElement() {
    String html = "<form>" +
                  "<select name=\"color\">" +
                  "<option value=\"red\">Red</option>" +
                  "</select>" +
                  "<input type=\"text\" name=\"id\">" +
                  "</form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.selectFirst("form");
    Element select = form.selectFirst("select[name=color]");
    select.remove();
    List<Connection.KeyVal> data = form.formData();
    assertEquals(1, data.size());
    assertEquals("id", data.get(0).key());
}
