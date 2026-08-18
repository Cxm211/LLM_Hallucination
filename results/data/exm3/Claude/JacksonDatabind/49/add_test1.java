// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java
public void testMultipleDistinctObjects() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    Foo mo = new Foo();
    mo.bar1 = new Bar();
    mo.bar2 = new Bar();

    String json = mapper.writeValueAsString(mo);
    Foo result = mapper.readValue(json, Foo.class);
    assertNotNull(result);
    assertNotSame(result.bar1, result.bar2);
}