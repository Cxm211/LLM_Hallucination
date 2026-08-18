// com/fasterxml/jackson/databind/mixins/MixinsWithBundlesTest.java
public void testMixinWithBundlesMultipleAnnotations() throws Exception
{
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(Foo.class, FooMixin.class);
    mapper.addMixIn(Foo.class, FooMixin.class);
    String result = mapper.writeValueAsString(new Foo("test"));
    assertEquals("{\"bar\":\"test\"}", result);
}