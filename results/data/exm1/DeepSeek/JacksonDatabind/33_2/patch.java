    public PropertyName findNameForSerialization(Annotated a)
    {
        String name = null;

        JsonGetter jg = _findAnnotation(a, JsonGetter.class);
        if (jg != null) {
            name = jg.value();
        } else {
            JsonProperty pann = _findAnnotation(a, JsonProperty.class);
            if (pann != null) {
                name = pann.value();
            } else if (_hasAnnotation(a, JsonSerialize.class)
                    || _hasAnnotation(a, JsonView.class)
                    || _hasAnnotation(a, JsonRawValue.class)) {
                return null;
            } else {
                return null;
            }
        }
        return PropertyName.construct(name);
    }