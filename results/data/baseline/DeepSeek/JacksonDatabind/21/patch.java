    public boolean isAnnotationBundle(Annotation ann) {
        if (ann == null) {
            return false;
        }
        return ann.annotationType().getAnnotation(JacksonAnnotationsInside.class) != null;
    }