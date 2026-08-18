// com/fasterxml/jackson/databind/mixins/MixinsWithBundlesTest.java::testMixinOverride
public void testMixinOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // First apply a mix-in that renames to 'alt'
        class FooMixinAlt {
            @com.fasterxml.jackson.annotation.JsonProperty("alt")
            public String bar;
        }
        mapper.addMixIn(Foo.class, FooMixinAlt.class);
        // Then override with the original mix-in that should rename to 'bar'
        mapper.addMixIn(Foo.class, FooMixin.class);
        String result = mapper.writeValueAsString(new Foo("result"));
        assertEquals("{\"bar\":\"result\"}", result);
    }