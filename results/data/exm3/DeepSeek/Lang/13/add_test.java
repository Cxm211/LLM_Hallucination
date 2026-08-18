// org/apache/commons/lang3/SerializationUtilsTest.java
public void testCustomObjectWithPrimitiveClassField() {
    class MyClass implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Class<?> clazz;
        MyClass(Class<?> c) { clazz = c; }
        public Class<?> getClazz() { return clazz; }
    }
    MyClass original = new MyClass(int.class);
    MyClass cloned = org.apache.commons.lang3.SerializationUtils.clone(original);
    org.junit.Assert.assertEquals(int.class, cloned.getClazz());
}
