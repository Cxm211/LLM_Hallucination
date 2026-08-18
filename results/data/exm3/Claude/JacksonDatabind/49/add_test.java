// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java
public void testMultipleReferencesToSameObject() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    Foo mo = new Foo();
    mo.bar1 = new Bar();
    mo.bar2 = mo.bar1;
    mo.bar3 = mo.bar1;

    String json = mapper.writeValueAsString(mo);
    Foo result = mapper.readValue(json, Foo.class);
    assertNotNull(result);
    assertSame(result.bar1, result.bar2);
    assertSame(result.bar1, result.bar3);
}