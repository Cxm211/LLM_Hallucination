// ===== FIXED com.fasterxml.jackson.databind.deser.CreatorProperty :: withValueDeserializer(JsonDeserializer) [lines 130-137] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-111-fixed/src/main/java/com/fasterxml/jackson/databind/deser/CreatorProperty.java =====
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
        return new CreatorProperty(this, deser, nvp);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.impl.FieldProperty :: withValueDeserializer(JsonDeserializer) [lines 88-95] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-111-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/FieldProperty.java =====
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
        return new FieldProperty(this, deser, nvp);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.impl.MethodProperty :: withValueDeserializer(JsonDeserializer) [lines 79-86] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-111-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/MethodProperty.java =====
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
        return new MethodProperty(this, deser, nvp);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.impl.ObjectIdReferenceProperty :: withValueDeserializer(JsonDeserializer) [lines 49-56] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-111-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/ObjectIdReferenceProperty.java =====
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
        return new ObjectIdReferenceProperty(this, deser, nvp);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty :: withValueDeserializer(JsonDeserializer) [lines 50-57] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-111-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/ObjectIdValueProperty.java =====
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
        return new ObjectIdValueProperty(this, deser, nvp);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.impl.SetterlessProperty :: withValueDeserializer(JsonDeserializer) [lines 63-70] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-111-fixed/src/main/java/com/fasterxml/jackson/databind/deser/impl/SetterlessProperty.java =====
    public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        NullValueProvider nvp = (_valueDeserializer == _nullProvider) ? deser : _nullProvider;
        return new SetterlessProperty(this, deser, nvp);
    }

// ===== FIXED com.fasterxml.jackson.databind.deser.std.AtomicReferenceDeserializer :: getNullValue(DeserializationContext) [lines 42-44] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-111-fixed/src/main/java/com/fasterxml/jackson/databind/deser/std/AtomicReferenceDeserializer.java =====
    public AtomicReference<Object> getNullValue(DeserializationContext ctxt) throws JsonMappingException {
        return new AtomicReference<Object>(_valueDeserializer.getNullValue(ctxt));
    }
