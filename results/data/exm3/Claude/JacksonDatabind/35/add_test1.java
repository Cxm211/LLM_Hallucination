// com/fasterxml/jackson/databind/jsontype/WrapperObjectWithObjectIdTest.java
public void testSingleComputer() throws Exception
{
    Company comp = new Company();
    comp.addComputer(new DesktopComputer("single-pc", "Tokyo"));
    final ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(comp);
    Company result = mapper.readValue(json, Company.class);
    assertNotNull(result);
    assertNotNull(result.computers);
    assertEquals(1, result.computers.size());
    assertTrue(result.computers.get(0) instanceof DesktopComputer);
}