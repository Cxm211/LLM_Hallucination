public boolean isAnnotationBundle(Annotation ann) {
        if (ann == null) {
            return false;
        }
        Class<? extends Annotation> type = ann.annotationType();
        return (type != null) && type.isAnnotationPresent(JacksonAnnotationsInside.class);
    }