// com/fasterxml/jackson/databind/ser/TestJsonSerialize.java
// Additional test for main type specialization
    public static class SuperClassForTest {
        public int value = 42;
    }

    @JsonSerialize(as=SubClassForTest.class)
    public static class AnnotatedSuperClassForTest extends SuperClassForTest {
        // empty
    }

    public static class SubClassForTest extends AnnotatedSuperClassForTest {
        // empty
    }

    public void testMainTypeSpecialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new AnnotatedSuperClassForTest());
        assertNotNull(json);
    }
