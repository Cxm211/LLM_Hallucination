// com/fasterxml/jackson/databind/struct/TestUnwrapped.java::testUnwrappedOnGetterAsPropertyIndicator
public void testUnwrappedOnGetterAsPropertyIndicator() throws Exception {
        class Inner2 { public String animal; }
        class Outer2 {
            private Inner2 inner;
            @com.fasterxml.jackson.annotation.JsonUnwrapped
            public Inner2 getInner() { return inner; }
            public void setInner(Inner2 v) { inner = v; }
        }
        Inner2 inner = new Inner2();
        inner.animal = "Zebra";
        Outer2 outer = new Outer2();
        outer.setInner(inner);
        String actual = MAPPER.writeValueAsString(outer);
        assertTrue(actual.contains("animal"));
        assertTrue(actual.contains("Zebra"));
        assertFalse(actual.contains("inner"));
    }