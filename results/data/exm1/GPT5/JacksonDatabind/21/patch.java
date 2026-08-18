public boolean isAnnotationBundle(Annotation ann) {
        if (ann == null) {
            return false;
        }
        return ann.annotationType().isAnnotationPresent(JacksonAnnotationsInside.class);
    }