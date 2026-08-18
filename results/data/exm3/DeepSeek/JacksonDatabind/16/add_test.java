// com/fasterxml/jackson/databind/mixins/MixinsWithBundlesTest.java
// Helper class to expose protected _add method
    static class TestAnnotationMap extends com.fasterxml.jackson.databind.introspect.AnnotationMap {
        public boolean addAnnotation(Annotation ann) {
            return _add(ann);
        }
    }

    public void testAnnotationAddNew() throws Exception {
        TestAnnotationMap map = new TestAnnotationMap();
        @com.fasterxml.jackson.annotation.JsonIgnore
        class IgnoredClass {}
        com.fasterxml.jackson.annotation.JsonIgnore ann = IgnoredClass.class.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class);
        assertNotNull(ann);
        boolean added = map.addAnnotation(ann);
        assertTrue(added);
        Annotation retrieved = map.get(com.fasterxml.jackson.annotation.JsonIgnore.class);
        assertNotNull(retrieved);
        assertEquals(ann, retrieved);
    }

    public void testAnnotationAddDifferent() throws Exception {
        TestAnnotationMap map = new TestAnnotationMap();
        @com.fasterxml.jackson.annotation.JsonIgnore(true)
        class IgnoredTrue {}
        com.fasterxml.jackson.annotation.JsonIgnore ann1 = IgnoredTrue.class.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class);
        assertNotNull(ann1);
        boolean added1 = map.addAnnotation(ann1);
        assertTrue(added1);
        @com.fasterxml.jackson.annotation.JsonIgnore(false)
        class IgnoredFalse {}
        com.fasterxml.jackson.annotation.JsonIgnore ann2 = IgnoredFalse.class.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class);
        assertNotNull(ann2);
        boolean added2 = map.addAnnotation(ann2);
        assertTrue(added2);
        Annotation retrieved = map.get(com.fasterxml.jackson.annotation.JsonIgnore.class);
        assertNotNull(retrieved);
        assertEquals(ann2, retrieved);
        assertFalse(ann1.equals(ann2));
    }

    public void testAnnotationAddDuplicate() throws Exception {
        TestAnnotationMap map = new TestAnnotationMap();
        @com.fasterxml.jackson.annotation.JsonIgnore(true)
        class IgnoredTrue {}
        com.fasterxml.jackson.annotation.JsonIgnore ann = IgnoredTrue.class.getAnnotation(com.fasterxml.jackson.annotation.JsonIgnore.class);
        assertNotNull(ann);
        boolean added1 = map.addAnnotation(ann);
        assertTrue(added1);
        boolean added2 = map.addAnnotation(ann);
        assertFalse(added2);
        Annotation retrieved = map.get(com.fasterxml.jackson.annotation.JsonIgnore.class);
        assertNotNull(retrieved);
        assertEquals(ann, retrieved);
    }
