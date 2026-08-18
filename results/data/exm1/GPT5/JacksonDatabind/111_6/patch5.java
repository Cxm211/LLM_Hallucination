public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // 07-May-2019, tatu: As per [databind#2303], must keep VD/NVP in-sync if they were
        NullValueProvider nvp = _nullProvider;
        if (nvp == _valueDeserializer) {
            nvp = deser;
        }
        return new SetterlessProperty(this, deser, nvp);
    }