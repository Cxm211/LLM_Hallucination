// com/fasterxml/jackson/databind/introspect/TestMixinMerging.java::testChildOverridesParentForRealMethod
public void testChildOverridesParentForRealMethod() throws Exception {
        class Bean { public int getX() { return 3; } }
        interface MixinParent { @com.fasterxml.jackson.annotation.JsonProperty("a") int getX(); }
        interface MixinChild extends MixinParent { @com.fasterxml.jackson.annotation.JsonProperty("x") int getX(); }

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("Test2");
        module.setMixInAnnotation(Bean.class, MixinChild.class);
        mapper.registerModule(module);

        assertEquals("{\"x\":3}", mapper.writeValueAsString(new Bean()));
    }