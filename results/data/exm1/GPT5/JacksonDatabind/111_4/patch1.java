public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        NullValueProvider nvp = _nullProvider;
        if (nvp == _valueDeserializer) {
            nvp = deser;
        }
        return new FieldProperty(this, deser, nvp);
    }