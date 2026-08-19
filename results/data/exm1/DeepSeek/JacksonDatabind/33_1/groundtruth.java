// ===== FIXED com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector :: findNameForSerialization(Annotated) [lines 731-758] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-33-fixed/src/main/java/com/fasterxml/jackson/databind/introspect/JacksonAnnotationIntrospector.java =====
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
                /* 22-Apr-2014, tatu: Should figure out a better way to do this, but
                 *   it's actually bit tricky to do it more efficiently (meta-annotations
                 *   add more lookups; AnnotationMap costs etc)
                 */
            } else if (_hasAnnotation(a, JsonSerialize.class)
                    || _hasAnnotation(a, JsonView.class)
                    || _hasAnnotation(a, JsonRawValue.class)
                    || _hasAnnotation(a, JsonUnwrapped.class)
                    || _hasAnnotation(a, JsonBackReference.class)
                    || _hasAnnotation(a, JsonManagedReference.class)) {
                name = "";
            } else {
                return null;
            }
        }
        return PropertyName.construct(name);
    }
