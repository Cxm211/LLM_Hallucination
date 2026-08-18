// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
public void testUnwrappedWithPrefix() throws Exception
    {
        class Inner {
            public String field = "value";
        }
        class Outer {
            @com.fasterxml.jackson.annotation.JsonUnwrapped(prefix="pre_")
            public Inner inner = new Inner();
        }
        Outer outer = new Outer();
        String json = MAPPER.writeValueAsString(outer);
        assertFalse(json.contains("\"inner\""));
        assertTrue(json.contains("\"pre_field\""));
        assertTrue(json.contains("\"value\""));
    }
