// com/fasterxml/jackson/databind/struct/TestUnwrapped.java
public void testUnwrappedOnGetter() throws Exception
    {
        class Inner {
            public String value = "test";
        }
        class Outer {
            private Inner inner = new Inner();
            @com.fasterxml.jackson.annotation.JsonUnwrapped
            public Inner getInner() { return inner; }
        }
        Outer outer = new Outer();
        String json = MAPPER.writeValueAsString(outer);
        assertFalse(json.contains("\"inner\""));
        assertTrue(json.contains("\"value\""));
        assertTrue(json.contains("\"test\""));
    }
