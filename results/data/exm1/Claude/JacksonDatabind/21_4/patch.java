public boolean isAnnotationBundle(Annotation ann) {
    return ann != null && ann.annotationType().getAnnotation(JacksonAnnotationsInside.class) != null;
}