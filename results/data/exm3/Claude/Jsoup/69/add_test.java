// org/jsoup/nodes/FormElementTest.java
@Test public void addElementNotInFormShouldNotBeAdded() {
    String html = "<html><body><form action='/test' method='post'></form><input type='text' name='outside' /></body></html>";
    Document doc = Jsoup.parse(html);
    FormElement form = (FormElement) doc.selectFirst("form");
    Element outsideInput = doc.selectFirst("input[name=outside]");
    
    form.addElement(outsideInput);
    
    List<Connection.KeyVal> data = form.formData();
    assertEquals(0, data.size());
}