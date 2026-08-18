// com/fasterxml/jackson/databind/deser/jdk/JDKAtomicTypesDeserTest.java
public void testNullWithinNestedDifferentNullProvider() throws Exception
{
    final ObjectReader r = MAPPER.readerFor(MyBean2303.class);
    MyBean2303 nullRef = r.readValue(" {\"refRef\": null } ");
    assertNotNull(nullRef.refRef);
    assertNotNull(nullRef.refRef.get());
    assertNull(nullRef.refRef.get().get());
    
    MyBean2303 intRef = r.readValue(" {\"refRef\": 2 } ");
    assertNotNull(intRef.refRef);
    assertNotNull(intRef.refRef.get());
    assertEquals(intRef.refRef.get().get(), new Integer(2));
}