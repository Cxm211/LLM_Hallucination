// com/fasterxml/jackson/databind/jsontype/WrapperObjectWithObjectIdTest.java
public void testEmptyCompany() throws Exception
{
    Company comp = new Company();
    final ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(comp);
    Company result = mapper.readValue(json, Company.class);
    assertNotNull(result);
    assertNotNull(result.computers);
    assertEquals(0, result.computers.size());
}