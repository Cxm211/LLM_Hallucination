// com/fasterxml/jackson/databind/creators/Creator1476Test.java
public void testSubclassCreatorOverride() throws Exception {
        // Test that a creator from a subclass overrides one from superclass.
        // We'll use a simple hierarchy with property creators.
        static class SuperClass {
            protected int superVal;
            @JsonCreator
            public SuperClass(@JsonProperty("superVal") int v) {
                this.superVal = v;
            }
        }
        static class SubClass extends SuperClass {
            private int subVal;
            @JsonCreator
            public SubClass(@JsonProperty("superVal") int v, @JsonProperty("subVal") int sv) {
                super(v);
                this.subVal = sv;
            }
            public int getSubVal() { return subVal; }
        }
        ObjectMapper mapper = new ObjectMapper();
        // Deserialize using SubClass - should use SubClass's creator
        SubClass sub = mapper.readValue("{ \"superVal\": 10, \"subVal\": 20 }", SubClass.class);
        assertEquals(10, sub.superVal);
        assertEquals(20, sub.getSubVal());
    }
