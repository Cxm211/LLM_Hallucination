// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java
public void testPropertyBasedIdNotFirst() throws Exception {
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
        static class Entity {
            public String name;
            public int id;
            public Entity(String name, int id) { this.name = name; this.id = id; }
        }
        static class Container {
            @JsonIdentityReference(alwaysAsId = true)
            public Entity first;
            public int other;
            public Entity second;
            Container(Entity a, int o, Entity b) { first = a; other = o; second = b; }
        }
        Entity e1 = new Entity("e1", 1);
        Entity e2 = new Entity("e2", 2);
        Container c = new Container(e1, 42, e2);
        String json = MAPPER.writeValueAsString(c);
        assertEquals(aposToQuotes("{'first':1,'other':42,'second':2}"), json);
    }
