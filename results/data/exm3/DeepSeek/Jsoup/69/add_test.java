// org/jsoup/nodes/FormElementTest.java
@Test public void removeTextareaFormElement() {
    String html = "<form action=\"/submit\">" +
                  "<textarea name=\"msg\">Hello</textarea>" +
                  "<input type=\"text\" name=\"user\">" +
                  "</form>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.selectFirst("form");
    Element textarea = form.selectFirst("textarea[name=msg]");
    textarea.remove();
    List<Connection.KeyVal> data = form.formData();
    assertEquals(1, data.size());
    assertEquals("user", data.get(0).key());
}
