// com/fasterxml/jackson/databind/ser/TestJsonSerializeAs.java::testMainTypeSpecialization
public void testMainTypeSpecialization() throws Exception {
        class Base { }
        class Sub extends Base { public int a = 1; }
        class Holder {
            @com.fasterxml.jackson.databind.annotation.JsonSerialize(as=Sub.class)
            public Base value = new Sub();
        }
        assertEquals(aposToQuotes("{'value':{'a':1}}"),
                WRITER.writeValueAsString(new Holder()));
    }