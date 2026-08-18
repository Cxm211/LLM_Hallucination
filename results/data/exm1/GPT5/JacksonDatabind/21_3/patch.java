public boolean isAnnotationBundle(Annotation ann) {
        return (ann != null) && ann.annotationType().isAnnotationPresent(JacksonAnnotationsInside.class);
    }