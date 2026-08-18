// com/fasterxml/jackson/databind/creators/Creator1476Test.java
public void testArrayDelegatingConstructor() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    
    class ArrayDelegatePojo {
        private int[] values;
        
        @JsonCreator
        public ArrayDelegatePojo(int[] values) {
            this.values = values;
        }
        
        public int[] getValues() { return values; }
    }
    
    ArrayDelegatePojo pojo = mapper.readValue("[1, 2, 3]", ArrayDelegatePojo.class);
    assertArrayEquals(new int[]{1, 2, 3}, pojo.getValues());
}