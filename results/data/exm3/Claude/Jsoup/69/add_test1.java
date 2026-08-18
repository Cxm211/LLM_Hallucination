// org/jsoup/nodes/FormElementTest.java
@Test public void removeAndReAddFormElement() {
    String html = "<html><body><form action='/test' method='post'><input type='text' name='field1' /><input type='text' name='field2' /></form></body></html>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.selectFirst("form");
    Element field1 = form.selectFirst("input[name=field1]");
    
    field1.remove();
    List<Connection.KeyVal> data = form.formData();
    assertEquals(1, data.size());
    assertEquals("field2", data.get(0).key());
    
    form.appendChild(field1);
    data = form.formData();
    assertEquals(2, data.size());
    assertEquals("field2", data.get(0).key());
    assertEquals("field1", data.get(1).key());
}