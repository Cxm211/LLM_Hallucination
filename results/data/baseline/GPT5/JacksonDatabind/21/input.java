// buggy code
    public boolean isAnnotationBundle(Annotation ann) {
        return ann.annotationType().getAnnotation(JacksonAnnotationsInside.class) != null;
    }

