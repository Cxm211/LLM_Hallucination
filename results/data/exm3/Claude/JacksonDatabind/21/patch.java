public boolean isAnnotationBundle(Annotation ann) {
    if (ann == null) {
        return false;
    }
    Class<? extends Annotation> annotationType = ann.annotationType();
    if (annotationType == null) {
        return false;
    }
    return annotationType.getAnnotation(JacksonAnnotationsInside.class) != null;
}