public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        // Keep ValueDeserializer and NullValueProvider in sync if previously linked
        NullValueProvider nvp = _nullProvider;
        if (nvp == _valueDeserializer) {
            nvp = deser;
        }
        return new ObjectIdReferenceProperty(this, deser, nvp);
    }