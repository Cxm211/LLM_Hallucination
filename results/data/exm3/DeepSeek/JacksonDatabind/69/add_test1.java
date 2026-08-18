// com/fasterxml/jackson/databind/creators/Creator1476Test.java
public void testDuplicatePropertyNamesThrows() {
        // This test expects an exception when duplicate property names are found.
        // We'll define a class with a creator that has duplicate @JsonProperty names.
        // The setup should fail during deserializer creation.
        static class DuplicateProps {
            private final int x, y;
            @JsonCreator
            public DuplicateProps(@JsonProperty("val") int x, @JsonProperty("val") int y) {
                this.x = x;
                this.y = y;
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{ \"val\": 1 }", DuplicateProps.class);
            fail("Should have thrown due to duplicate property names");
        } catch (IllegalArgumentException e) {
            // expected
            assertTrue(e.getMessage().contains("Duplicate creator property"));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
