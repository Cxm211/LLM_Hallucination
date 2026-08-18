// buggy function
    public static void addAll(Map<String, JsonSerializer<?>> allDeserializers)
    {
        final JsonSerializer<?> intS = new IntegerSerializer();
        allDeserializers.put(Integer.class.getName(), intS);
        allDeserializers.put(Integer.TYPE.getName(), intS);
        allDeserializers.put(Long.class.getName(), LongSerializer.instance);
        allDeserializers.put(Long.TYPE.getName(), LongSerializer.instance);
        allDeserializers.put(Byte.class.getName(), IntLikeSerializer.instance);
        allDeserializers.put(Byte.TYPE.getName(), IntLikeSerializer.instance);
        allDeserializers.put(Short.class.getName(), ShortSerializer.instance);
        allDeserializers.put(Short.TYPE.getName(), ShortSerializer.instance);

        // Numbers, limited length floating point
        allDeserializers.put(Float.class.getName(), FloatSerializer.instance);
        allDeserializers.put(Float.TYPE.getName(), FloatSerializer.instance);
        allDeserializers.put(Double.class.getName(), DoubleSerializer.instance);
        allDeserializers.put(Double.TYPE.getName(), DoubleSerializer.instance);
    }

        public JsonSerializer<?> createContextual(SerializerProvider prov,
                BeanProperty property) throws JsonMappingException
        {
            if (property != null) {
                AnnotatedMember m = property.getMember();
                if (m != null) {
                    JsonFormat.Value format = prov.getAnnotationIntrospector().findFormat(m);
                    if (format != null) {
                        switch (format.getShape()) {
                        case STRING:
                            return ToStringSerializer.instance;
                        default:
                        }
                    }
                }
            }
            return this;
        }

        public ShortSerializer() { super(Short.class, JsonParser.NumberType.INT, "number"); }

        public void serializeWithType(Object value, JsonGenerator gen,
                SerializerProvider provider, TypeSerializer typeSer) throws IOException {
            // no type info, just regular serialization
            serialize(value, gen, provider);            
        }

        public IntLikeSerializer() {
            super(Number.class, JsonParser.NumberType.INT, "integer");
        }

        public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(value.intValue());
        }

        public LongSerializer() { super(Long.class, JsonParser.NumberType.LONG, "number"); }

        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(((Long) value).longValue());
        }

        public FloatSerializer() { super(Float.class, JsonParser.NumberType.FLOAT, "number"); }

        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNumber(((Float) value).floatValue());
        }

        public DoubleSerializer() { super(Double.class, JsonParser.NumberType.DOUBLE, "number"); }

// trigger testcase
// com/fasterxml/jackson/databind/ser/TestJsonSerialize2.java::testEmptyInclusionScalars
public void testEmptyInclusionScalars() throws IOException
    {
        ObjectMapper defMapper = MAPPER;
        ObjectMapper inclMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        // First, Strings
        StringWrapper str = new StringWrapper("");
        assertEquals("{\"str\":\"\"}", defMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(str));
        assertEquals("{}", inclMapper.writeValueAsString(new StringWrapper()));

        assertEquals("{\"value\":\"x\"}", defMapper.writeValueAsString(new NonEmptyString("x")));
        assertEquals("{}", defMapper.writeValueAsString(new NonEmptyString("")));

        // Then numbers
        assertEquals("{\"value\":12}", defMapper.writeValueAsString(new NonEmptyInt(12)));
        assertEquals("{}", defMapper.writeValueAsString(new NonEmptyInt(0)));

        assertEquals("{\"value\":1.25}", defMapper.writeValueAsString(new NonEmptyDouble(1.25)));
        assertEquals("{}", defMapper.writeValueAsString(new NonEmptyDouble(0.0)));

        IntWrapper zero = new IntWrapper(0);
        assertEquals("{\"i\":0}", defMapper.writeValueAsString(zero));
        assertEquals("{}", inclMapper.writeValueAsString(zero));
    }
