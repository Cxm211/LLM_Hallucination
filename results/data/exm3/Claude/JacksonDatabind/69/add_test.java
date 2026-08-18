// com/fasterxml/jackson/databind/creators/Creator1476Test.java
public void testDelegatingConstructorChoice() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    
    @JsonDeserialize(builder = DelegateBuilder.class)
    class DelegatePojo {
        private int value;
        
        @JsonCreator
        public DelegatePojo(int value) {
            this.value = value;
        }
        
        public int getValue() { return value; }
    }
    
    class DelegateBuilder {
        @JsonCreator
        public DelegateBuilder(String str) {
            // delegate constructor
        }
        
        public DelegatePojo build() {
            return new DelegatePojo(42);
        }
    }
    
    DelegatePojo pojo = mapper.readValue("\"test\"", DelegatePojo.class);
    assertEquals(42, pojo.getValue());
}