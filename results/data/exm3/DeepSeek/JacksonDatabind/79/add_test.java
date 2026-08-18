// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java
public void testAlwaysAsIdFalse() throws Exception {
        @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
        static class ClassWithId {
            public int id;
            public String name;
            ClassWithId(int id, String name) { this.id = id; this.name = name; }
        }
        static class Container {
            @JsonIdentityReference(alwaysAsId = false)
            public ClassWithId ref;
            Container(ClassWithId ref) { this.ref = ref; }
        }
        ClassWithId obj = new ClassWithId(1, "test");
        Container c = new Container(obj);
        String json = MAPPER.writeValueAsString(c);
        assertEquals(aposToQuotes("{'ref':{'id':1,'name':'test'}}"), json);
    }
