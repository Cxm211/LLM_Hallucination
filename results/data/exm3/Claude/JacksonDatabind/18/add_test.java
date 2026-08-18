// com/fasterxml/jackson/databind/seq/ReadRecoveryTest.java
public void testArrayRecoveryWithMultipleErrors() throws Exception
{
    final String JSON = aposToQuotes("[{'a':1},{'unknown1':2},{'a':3},{'unknown2':4},{'a':5}]");
    
    MappingIterator<Bean> it = MAPPER.readerFor(Bean.class).readValues(JSON);
    
    // First bean should succeed
    assertTrue(it.hasNextValue());
    Bean bean = it.nextValue();
    assertEquals(1, bean.a);
    
    // Second bean should fail
    try {
        it.nextValue();
        fail("Should not have succeeded");
    } catch (JsonMappingException e) {
        verifyException(e, "Unrecognized field \"unknown1\"");
    }
    
    // Third bean should succeed after recovery
    assertTrue(it.hasNextValue());
    bean = it.nextValue();
    assertEquals(3, bean.a);
    
    // Fourth bean should fail
    try {
        it.nextValue();
        fail("Should not have succeeded");
    } catch (JsonMappingException e) {
        verifyException(e, "Unrecognized field \"unknown2\"");
    }
    
    // Fifth bean should succeed after second recovery
    assertTrue(it.hasNextValue());
    bean = it.nextValue();
    assertEquals(5, bean.a);
    
    assertFalse(it.hasNextValue());
    it.close();
}