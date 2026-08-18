// com/fasterxml/jackson/databind/struct/EnumFormatShapeTest.java
static class NaturalColorWrapper {
        @JsonFormat(shape = JsonFormat.Shape.NATURAL)
        public Color color;
        public NaturalColorWrapper() {}
        public NaturalColorWrapper(Color c) { color = c; }
    }

    public void testEnumPropertyAsNaturalWithIndex() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        String json = mapper.writeValueAsString(new NaturalColorWrapper(Color.GREEN));
        assertEquals(String.format(aposToQuotes("{'color':%s}"), Color.GREEN.ordinal()), json);
    }
