// com/fasterxml/jackson/databind/seq/ReadRecoveryTest.java
public void testRootRecoveryWithComplexNesting() throws Exception
{
    final String JSON = aposToQuotes("{'a':10}{'bad':{'nested':{'deep':'value'}}}{'a':20,'b':30}");
    
    MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
    
    // First bean should succeed
    Bean bean = it.nextValue();
    assertEquals(10, bean.a);
    
    // Second bean with nested structure should fail
    try {
        it.nextValue();
        fail("Should not have succeeded");
    } catch (JsonMappingException e) {
        verifyException(e, "Unrecognized field \"bad\"");
    }
    
    // Third bean should succeed after recovery
    assertTrue(it.hasNextValue());
    bean = it.nextValue();
    assertEquals(20, bean.a);
    assertEquals(30, bean.b);
    
    assertFalse(it.hasNextValue());
    it.close();
}