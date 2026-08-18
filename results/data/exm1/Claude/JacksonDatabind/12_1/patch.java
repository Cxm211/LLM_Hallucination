public boolean isCachable() {
    return (_valueTypeDeserializer == null)
            && (_ignorableProperties == null)
            && (_valueDeserializer == null)
            && (_keyDeserializer == null);
}