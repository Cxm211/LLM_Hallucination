// buggy function
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        return new CreatorProperty(this, deser, _nullProvider);
    }

    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        return new FieldProperty(this, deser, _nullProvider);
    }

    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        return new MethodProperty(this, deser, _nullProvider);
    }

    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        return new ObjectIdReferenceProperty(this, deser, _nullProvider);
    }

    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        return new ObjectIdValueProperty(this, deser, _nullProvider);
    }

    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        return new SetterlessProperty(this, deser, _nullProvider);
    }

    public AtomicReference<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return new AtomicReference<Object>();
    }

// trigger testcase
// com/fasterxml/jackson/databind/deser/jdk/JDKAtomicTypesDeserTest.java::testNullWithinNested
public void testNullWithinNested() throws Exception
    {
        final ObjectReader r = MAPPER.readerFor(MyBean2303.class);
        MyBean2303 intRef = r.readValue(" {\"refRef\": 2 } ");
        assertNotNull(intRef.refRef);
        assertNotNull(intRef.refRef.get());
        assertEquals(intRef.refRef.get().get(), new Integer(2));

        MyBean2303 nullRef = r.readValue(" {\"refRef\": null } ");
        assertNotNull(nullRef.refRef);
        assertNotNull(nullRef.refRef.get());
        assertNull(nullRef.refRef.get().get());
    }
