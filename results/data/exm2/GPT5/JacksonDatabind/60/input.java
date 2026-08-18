    public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer0) throws IOException
    {
        // Regardless of other parts, first need to find value to serialize:
        Object value = null;
        try {
            value = _accessorMethod.getValue(bean);
            // and if we got null, can also just write it directly
            if (value == null) {
                provider.defaultSerializeNull(gen);
                return;
            }
            JsonSerializer<Object> ser = _valueSerializer;
            if (ser == null) { // no serializer yet? Need to fetch
//                ser = provider.findTypedValueSerializer(value.getClass(), true, _property);
                ser = provider.findValueSerializer(value.getClass(), _property);
            } else {
                /* 09-Dec-2010, tatu: To work around natural type's refusal to add type info, we do
                 *    this (note: type is for the wrapper type, not enclosed value!)
                 */
                if (_forceTypeInformation) {
                    typeSer0.writeTypePrefixForScalar(bean, gen);
                    ser.serialize(value, gen, provider);
                    typeSer0.writeTypeSuffixForScalar(bean, gen);
                    return;
                }
            }
            // 28-Sep-2016, tatu: As per [databind#1385], we do need to do some juggling
            //    to use different Object for type id (logical type) and actual serialization
            //    (delegat type).
            ser.serializeWithType(value, gen, provider, typeSer0);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            Throwable t = e;
            // Need to unwrap this specific type, to see infinite recursion...
            while (t instanceof InvocationTargetException && t.getCause() != null) {
                t = t.getCause();
            }
            // Errors shouldn't be wrapped (and often can't, as well)
            if (t instanceof Error) {
                throw (Error) t;
            }
            // let's try to indicate the path best we can...
            throw JsonMappingException.wrapWithPath(t, bean, _accessorMethod.getName() + "()");
        }
    }

    public String toString() {
        return "(@JsonValue serializer for method " + _accessorMethod.getDeclaringClass() + "#" + _accessorMethod.getName() + ")";
    }

// trigger testcase
public void testWithCreatorAndJsonValue() throws Exception
    {
        final byte[] BYTES = new byte[] { 1, 2, 3, 4, 5 };
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();
        String json = mapper.writeValueAsString(new Bean1385Wrapper(
                new Bean1385(BYTES)
        ));
        Bean1385Wrapper result = mapper.readValue(json, Bean1385Wrapper.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(Bean1385.class, result.value.getClass());
        Bean1385 b = (Bean1385) result.value;
        Assert.assertArrayEquals(BYTES, b.raw);
    }

public void testWithAsValue() throws Exception
    {
        ExternalTypeWithNonPOJO input = new ExternalTypeWithNonPOJO(new AsValueThingy(12345L));
        String json = MAPPER.writeValueAsString(input);
        assertNotNull(json);
        assertEquals("{\"value\":12345,\"type\":\"thingy\"}", json);

        // and get it back too:
        ExternalTypeWithNonPOJO result = MAPPER.readValue(json, ExternalTypeWithNonPOJO.class);
        assertNotNull(result);
        assertNotNull(result.value);
        assertEquals(AsValueThingy.class, result.value.getClass());
        assertEquals(12345L, ((AsValueThingy) result.value).rawDate);
    }
