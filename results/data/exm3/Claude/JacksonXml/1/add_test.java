// com/fasterxml/jackson/dataformat/xml/lists/NestedUnwrappedLists180Test.java
public void testNestedUnwrappedListsMultipleEmpty() throws Exception
{
    String xml =
"<Records>\n"
+"<records></records>\n"
+"<records></records>\n"
+"<records>\n"
+"   <fields name='c'/>\n"
+"  </records>\n"
+"</Records>\n"
;

    Records result = MAPPER.readValue(xml, Records.class);
    assertNotNull(result.records);
    assertEquals(3, result.records.size());
    assertNotNull(result.records.get(0));
    assertNotNull(result.records.get(1));
    assertNotNull(result.records.get(2));
    assertEquals(0, result.records.get(0).fields.size());
    assertEquals(0, result.records.get(1).fields.size());
    assertEquals(1, result.records.get(2).fields.size());
    assertEquals("c", result.records.get(2).fields.get(0).name);
}