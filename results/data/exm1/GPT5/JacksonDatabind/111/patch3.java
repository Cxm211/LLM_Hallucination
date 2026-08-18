public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
        if (_valueDeserializer == deser) {
            return this;
        }
        NullValueProvider nvp = (deser == null) ? _nullProvider : deser;
        return new ObjectIdReferenceProperty(this, deser, nvp);
    }