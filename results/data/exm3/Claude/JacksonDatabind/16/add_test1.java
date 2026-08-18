// com/fasterxml/jackson/databind/mixins/MixinsWithBundlesTest.java
public void testMixinWithBundlesDifferentAnnotations() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(Foo.class, FooMixin.class);
    String result1 = mapper.writeValueAsString(new Foo("first"));
    assertEquals("{\"bar\":\"first\"}", result1);
    
    mapper = new ObjectMapper();
    mapper.addMixIn(Foo.class, FooMixin.class);
    String result2 = mapper.writeValueAsString(new Foo("second"));
    assertEquals("{\"bar\":\"second\"}", result2);
}