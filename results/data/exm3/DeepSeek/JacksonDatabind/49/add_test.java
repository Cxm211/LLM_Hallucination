// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java
@Test
    public void testGenerateIdForDifferentObjects() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = new Bar();
        String json = mapper.writeValueAsString(mo);
        Foo result = mapper.readValue(json, Foo.class);
        assertNotNull(result);
        assertNotSame(result.bar1, result.bar2);
    }
