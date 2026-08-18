// buggy code
    public static String getShortClassName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }
        if (className.length() == 0) {
            return StringUtils.EMPTY;
        }


        // Handle array encoding
            // Strip Object type encoding


        int lastDotIdx = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        int innerIdx = className.indexOf(
                INNER_CLASS_SEPARATOR_CHAR, lastDotIdx == -1 ? 0 : lastDotIdx + 1);
        String out = className.substring(lastDotIdx + 1);
        if (innerIdx != -1) {
            out = out.replace(INNER_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
        }
        return out;
    }

    public static String getPackageName(String className) {
        if (className == null) {
            return StringUtils.EMPTY;
        }

        // Strip array encoding
        // Strip Object type encoding

        int i = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        if (i == -1) {
            return StringUtils.EMPTY;
        }
        return className.substring(0, i);
    }

// relevant test
// org.apache.commons.lang.ClassUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new ClassUtils());
        Constructor[] cons = ClassUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(ClassUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ClassUtils.class.getModifiers()));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getShortClassName_Object
    public void test_getShortClassName_Object() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(new ClassUtils(), "<null>"));
        assertEquals("ClassUtilsTest.Inner", ClassUtils.getShortClassName(new Inner(), "<null>"));
        assertEquals("String", ClassUtils.getShortClassName("hello", "<null>"));
        assertEquals("<null>", ClassUtils.getShortClassName(null, "<null>"));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getShortClassName_Class
    public void test_getShortClassName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class));
        assertEquals("", ClassUtils.getShortClassName((Class) null));

        
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("Map.Entry[]", ClassUtils.getShortClassName(Map.Entry[].class));

        
        assertEquals("boolean", ClassUtils.getShortClassName(boolean.class));
        assertEquals("byte", ClassUtils.getShortClassName(byte.class));
        assertEquals("char", ClassUtils.getShortClassName(char.class));
        assertEquals("short", ClassUtils.getShortClassName(short.class));
        assertEquals("int", ClassUtils.getShortClassName(int.class));
        assertEquals("long", ClassUtils.getShortClassName(long.class));
        assertEquals("float", ClassUtils.getShortClassName(float.class));
        assertEquals("double", ClassUtils.getShortClassName(double.class));

        
        assertEquals("boolean[]", ClassUtils.getShortClassName(boolean[].class));
        assertEquals("byte[]", ClassUtils.getShortClassName(byte[].class));
        assertEquals("char[]", ClassUtils.getShortClassName(char[].class));
        assertEquals("short[]", ClassUtils.getShortClassName(short[].class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("long[]", ClassUtils.getShortClassName(long[].class));
        assertEquals("float[]", ClassUtils.getShortClassName(float[].class));
        assertEquals("double[]", ClassUtils.getShortClassName(double[].class));

        
        assertEquals("String[][]", ClassUtils.getShortClassName(String[][].class));
        assertEquals("String[][][]", ClassUtils.getShortClassName(String[][][].class));
        assertEquals("String[][][][]", ClassUtils.getShortClassName(String[][][][].class));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getShortClassName_String
    public void test_getShortClassName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortClassName(ClassUtils.class.getName()));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getPackageName_Object
    public void test_getPackageName_Object() {
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(new Inner(), "<null>"));
        assertEquals("<null>", ClassUtils.getPackageName(null, "<null>"));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getPackageName_Class
    public void test_getPackageName_Class() {
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class));
        assertEquals("", ClassUtils.getPackageName((Class)null));

        
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));

        
        assertEquals("", ClassUtils.getPackageName(boolean[].class));
        assertEquals("", ClassUtils.getPackageName(byte[].class));
        assertEquals("", ClassUtils.getPackageName(char[].class));
        assertEquals("", ClassUtils.getPackageName(short[].class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
        assertEquals("", ClassUtils.getPackageName(long[].class));
        assertEquals("", ClassUtils.getPackageName(float[].class));
        assertEquals("", ClassUtils.getPackageName(double[].class));

        
        assertEquals("java.lang", ClassUtils.getPackageName(String[][].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][][].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][][][].class));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getPackageName_String
    public void test_getPackageName_String() {
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(ClassUtils.class.getName()));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class.getName()));
        assertEquals("", ClassUtils.getPackageName((String)null));
        assertEquals("", ClassUtils.getPackageName(""));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getAllSuperclasses_Class
    public void test_getAllSuperclasses_Class() {
        List list = ClassUtils.getAllSuperclasses(CY.class);
        assertEquals(2, list.size());
        assertEquals(CX.class, list.get(0));
        assertEquals(Object.class, list.get(1));
        
        assertEquals(null, ClassUtils.getAllSuperclasses(null));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getAllInterfaces_Class
    public void test_getAllInterfaces_Class() {
        List list = ClassUtils.getAllInterfaces(CY.class);
        assertEquals(6, list.size());
        assertEquals(IB.class, list.get(0));
        assertEquals(IC.class, list.get(1));
        assertEquals(ID.class, list.get(2));
        assertEquals(IE.class, list.get(3));
        assertEquals(IF.class, list.get(4));
        assertEquals(IA.class, list.get(5));
        
        assertEquals(null, ClassUtils.getAllInterfaces(null));
    }

// org.apache.commons.lang.ClassUtilsTest::test_convertClassNamesToClasses_List
    public void test_convertClassNamesToClasses_List() {
        List list = new ArrayList();
        List result = ClassUtils.convertClassNamesToClasses(list);
        assertEquals(0, result.size());
        
        list.add("java.lang.String");
        list.add("java.lang.xxx");
        list.add("java.lang.Object");
        result = ClassUtils.convertClassNamesToClasses(list);
        assertEquals(3, result.size());
        assertEquals(String.class, result.get(0));
        assertEquals(null, result.get(1));
        assertEquals(Object.class, result.get(2));

        list.add(new Object());
        try {
            ClassUtils.convertClassNamesToClasses(list);
            fail();
        } catch (ClassCastException ex) {}
        assertEquals(null, ClassUtils.convertClassNamesToClasses(null));
    }

// org.apache.commons.lang.ClassUtilsTest::test_convertClassesToClassNames_List
    public void test_convertClassesToClassNames_List() {
        List list = new ArrayList();
        List result = ClassUtils.convertClassesToClassNames(list);
        assertEquals(0, result.size());
        
        list.add(String.class);
        list.add(null);
        list.add(Object.class);
        result = ClassUtils.convertClassesToClassNames(list);
        assertEquals(3, result.size());
        assertEquals("java.lang.String", result.get(0));
        assertEquals(null, result.get(1));
        assertEquals("java.lang.Object", result.get(2));

        list.add(new Object());
        try {
            ClassUtils.convertClassesToClassNames(list);
            fail();
        } catch (ClassCastException ex) {}
        assertEquals(null, ClassUtils.convertClassesToClassNames(null));
    }

// org.apache.commons.lang.ClassUtilsTest::test_isInnerClass_Class
    public void test_isInnerClass_Class() {
        assertEquals(true, ClassUtils.isInnerClass(Inner.class));
        assertEquals(true, ClassUtils.isInnerClass(Map.Entry.class));
        assertEquals(true, ClassUtils.isInnerClass(new Cloneable() {
        }.getClass()));
        assertEquals(false, ClassUtils.isInnerClass(this.getClass()));
        assertEquals(false, ClassUtils.isInnerClass(String.class));
        assertEquals(false, ClassUtils.isInnerClass(null));
    }

// org.apache.commons.lang.ClassUtilsTest::test_isAssignable_ClassArray_ClassArray
    public void test_isAssignable_ClassArray_ClassArray() throws Exception {
        Class[] array2 = new Class[] {Object.class, Object.class};
        Class[] array1 = new Class[] {Object.class};
        Class[] array1s = new Class[] {String.class};
        Class[] array0 = new Class[] {};

        assertFalse(ClassUtils.isAssignable(array1, array2));
        assertFalse(ClassUtils.isAssignable(null, array2));
        assertTrue(ClassUtils.isAssignable(null, array0));
        assertTrue(ClassUtils.isAssignable(array0, array0));
        assertTrue(ClassUtils.isAssignable(array0, null));
        assertTrue(ClassUtils.isAssignable((Class[]) null, (Class[]) null));
        
        assertFalse(ClassUtils.isAssignable(array1, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1s));
        assertTrue(ClassUtils.isAssignable(array1s, array1));
    }

// org.apache.commons.lang.ClassUtilsTest::test_isAssignable
    public void test_isAssignable() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class) null, null));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        
        assertTrue(ClassUtils.isAssignable(null, Object.class));
        assertTrue(ClassUtils.isAssignable(null, Integer.class));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertTrue(ClassUtils.isAssignable(String.class, String.class));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class));
        assertFalse(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class));
    }

// org.apache.commons.lang.ClassUtilsTest::test_isAssignable_Autoboxing
    public void test_isAssignable_Autoboxing() throws Exception {
        assertFalse(ClassUtils.isAssignable((Class) null, null, true));
        assertFalse(ClassUtils.isAssignable(String.class, null, true));

        assertTrue(ClassUtils.isAssignable(null, Object.class, true));
        assertTrue(ClassUtils.isAssignable(null, Integer.class, true));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class, true));
        assertTrue(ClassUtils.isAssignable(String.class, String.class, true));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.class, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Boolean.class, Boolean.class, true));
    }

// org.apache.commons.lang.ClassUtilsTest::test_isAssignable_Widening
    public void test_isAssignable_Widening() throws Exception {
        
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.TYPE, Character.TYPE));
        assertTrue("byte -> byte", ClassUtils.isAssignable(Byte.TYPE, Byte.TYPE));
        assertTrue("byte -> short", ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue("byte -> int", ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        assertTrue("byte -> long", ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        assertTrue("byte -> float", ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        assertTrue("byte -> double", ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.TYPE, Boolean.TYPE));
        
        
        assertFalse("short -> char", ClassUtils.isAssignable(Short.TYPE, Character.TYPE));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.TYPE, Byte.TYPE));
        assertTrue("short -> short", ClassUtils.isAssignable(Short.TYPE, Short.TYPE));
        assertTrue("short -> int", ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue("short -> long", ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        assertTrue("short -> float", ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        assertTrue("short -> double", ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.TYPE, Boolean.TYPE));
        
        
        assertTrue("char -> char", ClassUtils.isAssignable(Character.TYPE, Character.TYPE));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.TYPE, Byte.TYPE));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.TYPE, Short.TYPE));
        assertTrue("char -> int", ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue("char -> long", ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        assertTrue("char -> float", ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        assertTrue("char -> double", ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.TYPE, Boolean.TYPE));
        
        
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.TYPE, Character.TYPE));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.TYPE, Byte.TYPE));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));
        assertTrue("int -> int", ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue("int -> long", ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue("int -> float", ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue("int -> double", ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.TYPE, Boolean.TYPE));
 
        
        assertFalse("long -> char", ClassUtils.isAssignable(Long.TYPE, Character.TYPE));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.TYPE, Byte.TYPE));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.TYPE, Short.TYPE));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));
        assertTrue("long -> long", ClassUtils.isAssignable(Long.TYPE, Long.TYPE));
        assertTrue("long -> float", ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        assertTrue("long -> double", ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.TYPE, Boolean.TYPE));
 
        
        assertFalse("float -> char", ClassUtils.isAssignable(Float.TYPE, Character.TYPE));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.TYPE, Byte.TYPE));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.TYPE, Short.TYPE));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.TYPE, Integer.TYPE));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.TYPE, Long.TYPE));
        assertTrue("float -> float", ClassUtils.isAssignable(Float.TYPE, Float.TYPE));
        assertTrue("float -> double", ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.TYPE, Boolean.TYPE));
        
        
        assertFalse("double -> char", ClassUtils.isAssignable(Double.TYPE, Character.TYPE));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.TYPE, Byte.TYPE));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.TYPE, Short.TYPE));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.TYPE, Integer.TYPE));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.TYPE, Long.TYPE));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
        assertTrue("double -> double", ClassUtils.isAssignable(Double.TYPE, Double.TYPE));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.TYPE, Boolean.TYPE));
        
        
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.TYPE, Character.TYPE));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.TYPE, Byte.TYPE));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.TYPE, Short.TYPE));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.TYPE, Long.TYPE));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.TYPE, Float.TYPE));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.TYPE, Double.TYPE));
        assertTrue("boolean -> boolean", ClassUtils.isAssignable(Boolean.TYPE, Boolean.TYPE));
    }

// org.apache.commons.lang.ClassUtilsTest::test_isAssignable_Unboxing_Widening
    public void test_isAssignable_Unboxing_Widening() throws Exception {
        
        assertFalse("byte -> char", ClassUtils.isAssignable(Byte.class, Character.TYPE, true));
        assertTrue("byte -> byte", ClassUtils.isAssignable(Byte.class, Byte.TYPE, true));
        assertTrue("byte -> short", ClassUtils.isAssignable(Byte.class, Short.TYPE, true));
        assertTrue("byte -> int", ClassUtils.isAssignable(Byte.class, Integer.TYPE, true));
        assertTrue("byte -> long", ClassUtils.isAssignable(Byte.class, Long.TYPE, true));
        assertTrue("byte -> float", ClassUtils.isAssignable(Byte.class, Float.TYPE, true));
        assertTrue("byte -> double", ClassUtils.isAssignable(Byte.class, Double.TYPE, true));
        assertFalse("byte -> boolean", ClassUtils.isAssignable(Byte.class, Boolean.TYPE, true));
        
        
        assertFalse("short -> char", ClassUtils.isAssignable(Short.class, Character.TYPE, true));
        assertFalse("short -> byte", ClassUtils.isAssignable(Short.class, Byte.TYPE, true));
        assertTrue("short -> short", ClassUtils.isAssignable(Short.class, Short.TYPE, true));
        assertTrue("short -> int", ClassUtils.isAssignable(Short.class, Integer.TYPE, true));
        assertTrue("short -> long", ClassUtils.isAssignable(Short.class, Long.TYPE, true));
        assertTrue("short -> float", ClassUtils.isAssignable(Short.class, Float.TYPE, true));
        assertTrue("short -> double", ClassUtils.isAssignable(Short.class, Double.TYPE, true));
        assertFalse("short -> boolean", ClassUtils.isAssignable(Short.class, Boolean.TYPE, true));
        
        
        assertTrue("char -> char", ClassUtils.isAssignable(Character.class, Character.TYPE, true));
        assertFalse("char -> byte", ClassUtils.isAssignable(Character.class, Byte.TYPE, true));
        assertFalse("char -> short", ClassUtils.isAssignable(Character.class, Short.TYPE, true));
        assertTrue("char -> int", ClassUtils.isAssignable(Character.class, Integer.TYPE, true));
        assertTrue("char -> long", ClassUtils.isAssignable(Character.class, Long.TYPE, true));
        assertTrue("char -> float", ClassUtils.isAssignable(Character.class, Float.TYPE, true));
        assertTrue("char -> double", ClassUtils.isAssignable(Character.class, Double.TYPE, true));
        assertFalse("char -> boolean", ClassUtils.isAssignable(Character.class, Boolean.TYPE, true));
        
        
        assertFalse("int -> char", ClassUtils.isAssignable(Integer.class, Character.TYPE, true));
        assertFalse("int -> byte", ClassUtils.isAssignable(Integer.class, Byte.TYPE, true));
        assertFalse("int -> short", ClassUtils.isAssignable(Integer.class, Short.TYPE, true));
        assertTrue("int -> int", ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue("int -> long", ClassUtils.isAssignable(Integer.class, Long.TYPE, true));
        assertTrue("int -> float", ClassUtils.isAssignable(Integer.class, Float.TYPE, true));
        assertTrue("int -> double", ClassUtils.isAssignable(Integer.class, Double.TYPE, true));
        assertFalse("int -> boolean", ClassUtils.isAssignable(Integer.class, Boolean.TYPE, true));
        
        
        assertFalse("long -> char", ClassUtils.isAssignable(Long.class, Character.TYPE, true));
        assertFalse("long -> byte", ClassUtils.isAssignable(Long.class, Byte.TYPE, true));
        assertFalse("long -> short", ClassUtils.isAssignable(Long.class, Short.TYPE, true));
        assertFalse("long -> int", ClassUtils.isAssignable(Long.class, Integer.TYPE, true));
        assertTrue("long -> long", ClassUtils.isAssignable(Long.class, Long.TYPE, true));
        assertTrue("long -> float", ClassUtils.isAssignable(Long.class, Float.TYPE, true));
        assertTrue("long -> double", ClassUtils.isAssignable(Long.class, Double.TYPE, true));
        assertFalse("long -> boolean", ClassUtils.isAssignable(Long.class, Boolean.TYPE, true));
        
        
        assertFalse("float -> char", ClassUtils.isAssignable(Float.class, Character.TYPE, true));
        assertFalse("float -> byte", ClassUtils.isAssignable(Float.class, Byte.TYPE, true));
        assertFalse("float -> short", ClassUtils.isAssignable(Float.class, Short.TYPE, true));
        assertFalse("float -> int", ClassUtils.isAssignable(Float.class, Integer.TYPE, true));
        assertFalse("float -> long", ClassUtils.isAssignable(Float.class, Long.TYPE, true));
        assertTrue("float -> float", ClassUtils.isAssignable(Float.class, Float.TYPE, true));
        assertTrue("float -> double", ClassUtils.isAssignable(Float.class, Double.TYPE, true));
        assertFalse("float -> boolean", ClassUtils.isAssignable(Float.class, Boolean.TYPE, true));
        
        
        assertFalse("double -> char", ClassUtils.isAssignable(Double.class, Character.TYPE, true));
        assertFalse("double -> byte", ClassUtils.isAssignable(Double.class, Byte.TYPE, true));
        assertFalse("double -> short", ClassUtils.isAssignable(Double.class, Short.TYPE, true));
        assertFalse("double -> int", ClassUtils.isAssignable(Double.class, Integer.TYPE, true));
        assertFalse("double -> long", ClassUtils.isAssignable(Double.class, Long.TYPE, true));
        assertFalse("double -> float", ClassUtils.isAssignable(Double.class, Float.TYPE, true));
        assertTrue("double -> double", ClassUtils.isAssignable(Double.class, Double.TYPE, true));
        assertFalse("double -> boolean", ClassUtils.isAssignable(Double.class, Boolean.TYPE, true));
        
        
        assertFalse("boolean -> char", ClassUtils.isAssignable(Boolean.class, Character.TYPE, true));
        assertFalse("boolean -> byte", ClassUtils.isAssignable(Boolean.class, Byte.TYPE, true));
        assertFalse("boolean -> short", ClassUtils.isAssignable(Boolean.class, Short.TYPE, true));
        assertFalse("boolean -> int", ClassUtils.isAssignable(Boolean.class, Integer.TYPE, true));
        assertFalse("boolean -> long", ClassUtils.isAssignable(Boolean.class, Long.TYPE, true));
        assertFalse("boolean -> float", ClassUtils.isAssignable(Boolean.class, Float.TYPE, true));
        assertFalse("boolean -> double", ClassUtils.isAssignable(Boolean.class, Double.TYPE, true));
        assertTrue("boolean -> boolean", ClassUtils.isAssignable(Boolean.class, Boolean.TYPE, true));
    }

// org.apache.commons.lang.ClassUtilsTest::testPrimitiveToWrapper
    public void testPrimitiveToWrapper() {
       
        
        assertEquals("boolean -> Boolean.class", 
            Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));   
        assertEquals("byte -> Byte.class",
            Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals("char -> Character.class",
            Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals("short -> Short.class",
            Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals("int -> Integer.class",
            Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals("long -> Long.class",
            Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals("double -> Double.class",
            Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals("float -> Float.class",
            Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        
        
        assertEquals("String.class -> String.class",
            String.class, ClassUtils.primitiveToWrapper(String.class));
        assertEquals("ClassUtils.class -> ClassUtils.class",
            org.apache.commons.lang.ClassUtils.class, 
            ClassUtils.primitiveToWrapper(org.apache.commons.lang.ClassUtils.class));
        assertEquals("Void.TYPE -> Void.TYPE",
            Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
            
        
        assertNull("null -> null",
            ClassUtils.primitiveToWrapper(null));
    }

// org.apache.commons.lang.ClassUtilsTest::testPrimitivesToWrappers
    public void testPrimitivesToWrappers() {
        
        assertNull("null -> null",
            ClassUtils.primitivesToWrappers(null));
        
        assertEquals("empty -> empty",
                ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.primitivesToWrappers(ArrayUtils.EMPTY_CLASS_ARRAY));

        
        final Class[] primitives = new Class[] {
                Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, 
                Integer.TYPE, Long.TYPE, Double.TYPE, Float.TYPE,
                String.class, ClassUtils.class
        };
        Class[] wrappers= ClassUtils.primitivesToWrappers(primitives);
        
        for (int i=0; i < primitives.length; i++) {
            
            Class primitive = primitives[i];
            Class expectedWrapper = ClassUtils.primitiveToWrapper(primitive);
            
            assertEquals(primitive + " -> " + expectedWrapper, expectedWrapper, wrappers[i]);
        }

        
        final Class[] noPrimitives = new Class[] {
                String.class, ClassUtils.class, Void.TYPE
        };
        
        assertNotSame("unmodified", noPrimitives, ClassUtils.primitivesToWrappers(noPrimitives));
    }

// org.apache.commons.lang.ClassUtilsTest::testWrapperToPrimitive
    public void testWrapperToPrimitive() {
        
        final Class[] primitives = {
                Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
                Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE
        };
        for (int i = 0; i < primitives.length; i++) {
            Class wrapperCls = ClassUtils.primitiveToWrapper(primitives[i]);
            assertFalse("Still primitive", wrapperCls.isPrimitive());
            assertEquals(wrapperCls + " -> " + primitives[i], primitives[i],
                    ClassUtils.wrapperToPrimitive(wrapperCls));
        }
    }

// org.apache.commons.lang.ClassUtilsTest::testWrapperToPrimitiveNoWrapper
    public void testWrapperToPrimitiveNoWrapper() {
        assertNull("Wrong result for non wrapper class", ClassUtils.wrapperToPrimitive(String.class));
    }

// org.apache.commons.lang.ClassUtilsTest::testWrapperToPrimitiveNull
    public void testWrapperToPrimitiveNull() {
        assertNull("Wrong result for null class", ClassUtils.wrapperToPrimitive(null));
    }

// org.apache.commons.lang.ClassUtilsTest::testWrappersToPrimitives
    public void testWrappersToPrimitives() {
        
        final Class[] classes = {
                Boolean.class, Byte.class, Character.class, Short.class,
                Integer.class, Long.class, Float.class, Double.class,
                String.class, ClassUtils.class, null
        };

        Class[] primitives = ClassUtils.wrappersToPrimitives(classes);
        
        assertEquals("Wrong length of result array", classes.length, primitives.length);
        for (int i = 0; i < classes.length; i++) {
            Class expectedPrimitive = ClassUtils.wrapperToPrimitive(classes[i]);
            assertEquals(classes[i] + " -> " + expectedPrimitive, expectedPrimitive,
                    primitives[i]);
        }
    }

// org.apache.commons.lang.ClassUtilsTest::testWrappersToPrimitivesNull
    public void testWrappersToPrimitivesNull() {
        assertNull("Wrong result for null input", ClassUtils.wrappersToPrimitives(null));
    }

// org.apache.commons.lang.ClassUtilsTest::testWrappersToPrimitivesEmpty
    public void testWrappersToPrimitivesEmpty() {
        Class[] empty = new Class[0];
        assertEquals("Wrong result for empty input", empty, ClassUtils.wrappersToPrimitives(empty));
    }

// org.apache.commons.lang.ClassUtilsTest::testGetClassClassNotFound
    public void testGetClassClassNotFound() throws Exception {
        assertGetClassThrowsClassNotFound( "bool" );
        assertGetClassThrowsClassNotFound( "bool[]" );
        assertGetClassThrowsClassNotFound( "integer[]" );
    }

// org.apache.commons.lang.ClassUtilsTest::testGetClassInvalidArguments
    public void testGetClassInvalidArguments() throws Exception {
        assertGetClassThrowsNullPointerException( null );
        assertGetClassThrowsClassNotFound( "[][][]" );
        assertGetClassThrowsClassNotFound( "[[]" );
        assertGetClassThrowsClassNotFound( "[" );
        assertGetClassThrowsClassNotFound( "java.lang.String][" );
        assertGetClassThrowsClassNotFound( ".hello.world" );
        assertGetClassThrowsClassNotFound( "hello..world" );
    }

// org.apache.commons.lang.ClassUtilsTest::testWithInterleavingWhitespace
    public void testWithInterleavingWhitespace() throws ClassNotFoundException {
        assertEquals( int[].class, ClassUtils.getClass( " int [ ] " ) );
        assertEquals( long[].class, ClassUtils.getClass( "\rlong\t[\n]\r" ) );
        assertEquals( short[].class, ClassUtils.getClass( "\tshort                \t\t[]" ) );
        assertEquals( byte[].class, ClassUtils.getClass( "byte[\t\t\n\r]   " ) );
    }

// org.apache.commons.lang.ClassUtilsTest::testGetClassByNormalNameArrays
    public void testGetClassByNormalNameArrays() throws ClassNotFoundException {
        assertEquals( int[].class, ClassUtils.getClass( "int[]" ) );
        assertEquals( long[].class, ClassUtils.getClass( "long[]" ) );
        assertEquals( short[].class, ClassUtils.getClass( "short[]" ) );
        assertEquals( byte[].class, ClassUtils.getClass( "byte[]" ) );
        assertEquals( char[].class, ClassUtils.getClass( "char[]" ) );
        assertEquals( float[].class, ClassUtils.getClass( "float[]" ) );
        assertEquals( double[].class, ClassUtils.getClass( "double[]" ) );
        assertEquals( boolean[].class, ClassUtils.getClass( "boolean[]" ) );
        assertEquals( String[].class, ClassUtils.getClass( "java.lang.String[]" ) );
    }

// org.apache.commons.lang.ClassUtilsTest::testGetClassByNormalNameArrays2D
    public void testGetClassByNormalNameArrays2D() throws ClassNotFoundException {
        assertEquals( int[][].class, ClassUtils.getClass( "int[][]" ) );
        assertEquals( long[][].class, ClassUtils.getClass( "long[][]" ) );
        assertEquals( short[][].class, ClassUtils.getClass( "short[][]" ) );
        assertEquals( byte[][].class, ClassUtils.getClass( "byte[][]" ) );
        assertEquals( char[][].class, ClassUtils.getClass( "char[][]" ) );
        assertEquals( float[][].class, ClassUtils.getClass( "float[][]" ) );
        assertEquals( double[][].class, ClassUtils.getClass( "double[][]" ) );
        assertEquals( boolean[][].class, ClassUtils.getClass( "boolean[][]" ) );
        assertEquals( String[][].class, ClassUtils.getClass( "java.lang.String[][]" ) );
    }

// org.apache.commons.lang.ClassUtilsTest::testGetClassWithArrayClasses2D
    public void testGetClassWithArrayClasses2D() throws Exception {
        assertGetClassReturnsClass( String[][].class );
        assertGetClassReturnsClass( int[][].class );
        assertGetClassReturnsClass( long[][].class );
        assertGetClassReturnsClass( short[][].class );
        assertGetClassReturnsClass( byte[][].class );
        assertGetClassReturnsClass( char[][].class );
        assertGetClassReturnsClass( float[][].class );
        assertGetClassReturnsClass( double[][].class );
        assertGetClassReturnsClass( boolean[][].class );
    }

// org.apache.commons.lang.ClassUtilsTest::testGetClassWithArrayClasses
    public void testGetClassWithArrayClasses() throws Exception {
        assertGetClassReturnsClass( String[].class );
        assertGetClassReturnsClass( int[].class );
        assertGetClassReturnsClass( long[].class );
        assertGetClassReturnsClass( short[].class );
        assertGetClassReturnsClass( byte[].class );
        assertGetClassReturnsClass( char[].class );
        assertGetClassReturnsClass( float[].class );
        assertGetClassReturnsClass( double[].class );
        assertGetClassReturnsClass( boolean[].class );
    }

// org.apache.commons.lang.ClassUtilsTest::testGetClassRawPrimitives
    public void testGetClassRawPrimitives() throws ClassNotFoundException {
        assertEquals( int.class, ClassUtils.getClass( "int" ) );
        assertEquals( long.class, ClassUtils.getClass( "long" ) );
        assertEquals( short.class, ClassUtils.getClass( "short" ) );
        assertEquals( byte.class, ClassUtils.getClass( "byte" ) );
        assertEquals( char.class, ClassUtils.getClass( "char" ) );
        assertEquals( float.class, ClassUtils.getClass( "float" ) );
        assertEquals( double.class, ClassUtils.getClass( "double" ) );
        assertEquals( boolean.class, ClassUtils.getClass( "boolean" ) );
    }

// org.apache.commons.lang.ClassUtilsTest::testShowJavaBug
    public void testShowJavaBug() throws Exception {
        
        Set set = Collections.unmodifiableSet(new HashSet());
        Method isEmptyMethod = set.getClass().getMethod("isEmpty",  new Class[0]);
        try {
            isEmptyMethod.invoke(set, new Object[0]);
            fail("Failed to throw IllegalAccessException as expected");
        } catch(IllegalAccessException iae) {
            
        }
    }

// org.apache.commons.lang.ClassUtilsTest::testGetPublicMethod
    public void testGetPublicMethod() throws Exception {
        
        Set set = Collections.unmodifiableSet(new HashSet());
        Method isEmptyMethod = ClassUtils.getPublicMethod(set.getClass(), "isEmpty",  new Class[0]);
            assertTrue(Modifier.isPublic(isEmptyMethod.getDeclaringClass().getModifiers()));
 
        try {
            isEmptyMethod.invoke(set, new Object[0]);
        } catch(java.lang.IllegalAccessException iae) {
            fail("Should not have thrown IllegalAccessException");
        }
               
        
        Method toStringMethod = ClassUtils.getPublicMethod(Object.class, "toString",  new Class[0]);
            assertEquals(Object.class.getMethod("toString", new Class[0]), toStringMethod);
    }

// org.apache.commons.lang.ClassUtilsTest::testToClass_object
    public void testToClass_object() {
        assertEquals(null, ClassUtils.toClass(null));

        assertSame(
            ArrayUtils.EMPTY_CLASS_ARRAY,
            ClassUtils.toClass(new Class[0]));

        Object[] array = new Object[3];
        array[0] = new String("Test");
        array[1] = new Integer(1);
        array[2] = new Double(99);

        Class[] results = ClassUtils.toClass(array);
        assertEquals("String", ClassUtils.getShortClassName(results[0]));
        assertEquals("Integer", ClassUtils.getShortClassName(results[1]));
        assertEquals("Double", ClassUtils.getShortClassName(results[2]));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getShortCanonicalName_Object
    public void test_getShortCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getShortCanonicalName(null, "<null>"));
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(new int[0], "<null>"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(new int[0][0], "<null>"));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getShortCanonicalName_Class
    public void test_getShortCanonicalName_Class() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName(ClassUtils.class));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName(ClassUtils[].class));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName(ClassUtils[][].class));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(int[][].class));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getShortCanonicalName_String
    public void test_getShortCanonicalName_String() {
        assertEquals("ClassUtils", ClassUtils.getShortCanonicalName("org.apache.commons.lang.ClassUtils"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName("[Lorg.apache.commons.lang.ClassUtils;"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName("[[Lorg.apache.commons.lang.ClassUtils;"));
        assertEquals("ClassUtils[]", ClassUtils.getShortCanonicalName("org.apache.commons.lang.ClassUtils[]"));
        assertEquals("ClassUtils[][]", ClassUtils.getShortCanonicalName("org.apache.commons.lang.ClassUtils[][]"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("[[I"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("int[]"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("int[][]"));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getPackageCanonicalName_Object
    public void test_getPackageCanonicalName_Object() {
        assertEquals("<null>", ClassUtils.getPackageCanonicalName(null, "<null>"));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(new ClassUtils(), "<null>"));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(new ClassUtils[0], "<null>"));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(new ClassUtils[0][0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0], "<null>"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0][0], "<null>"));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getPackageCanonicalName_Class
    public void test_getPackageCanonicalName_Class() {
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(ClassUtils.class));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(ClassUtils[].class));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(ClassUtils[][].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[][].class));
    }

// org.apache.commons.lang.ClassUtilsTest::test_getPackageCanonicalName_String
    public void test_getPackageCanonicalName_String() {
        assertEquals("org.apache.commons.lang", 
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang.ClassUtils"));
        assertEquals("org.apache.commons.lang", 
            ClassUtils.getPackageCanonicalName("[Lorg.apache.commons.lang.ClassUtils;"));
        assertEquals("org.apache.commons.lang", 
            ClassUtils.getPackageCanonicalName("[[Lorg.apache.commons.lang.ClassUtils;"));
        assertEquals("org.apache.commons.lang", 
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang.ClassUtils[]"));
        assertEquals("org.apache.commons.lang", 
            ClassUtils.getPackageCanonicalName("org.apache.commons.lang.ClassUtils[][]"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[[I"));
        assertEquals("", ClassUtils.getPackageCanonicalName("int[]"));
        assertEquals("", ClassUtils.getPackageCanonicalName("int[][]"));
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "John Doe";
        p.age = 33;
        p.smoker = false;
        String pBaseStr = p.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(p));
        assertEquals(pBaseStr + "[name=John Doe,age=33,smoker=false]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.DefaultToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "]").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]").toString());
        
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "]").append("a", "hello").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper("Integer@8888[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]").append("a", "hello").toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=hello" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "  b=4" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<Integer>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=[]" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a={}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=<size=0>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a={}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Jane Doe";
        p.age = 25;
        p.smoker = true;
        String pBaseStr = p.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(p));
        assertEquals(pBaseStr + "[" + SystemUtils.LINE_SEPARATOR + "  name=Jane Doe" + SystemUtils.LINE_SEPARATOR + "  age=25" + SystemUtils.LINE_SEPARATOR + "  smoker=true" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  a=3" + SystemUtils.LINE_SEPARATOR + "  b=4" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {<null>,5,{3,6}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {<null>,5,{3,6}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {1,2,-3,4}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {1,2,-3,4}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.MultiLineToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {{1,2},<null>,{5}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  {{1,2},<null>,{5}}" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[" + SystemUtils.LINE_SEPARATOR + "  <null>" + SystemUtils.LINE_SEPARATOR + "]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[3,4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[{}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[{}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Ron Paul";
        p.age = 72;
        p.smoker = false;
        String pBaseStr = p.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(p));
        assertEquals(pBaseStr + "[Ron Paul,72,false]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[3,4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.NoFieldNamesToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "John Q. Public";
        p.age = 45;
        p.smoker = true;
        String pBaseStr = "ToStringStyleTest.Person";
        assertEquals(pBaseStr + "[name=John Q. Public,age=45,smoker=true]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ShortPrefixToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals("", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals("", new ToStringBuilder(base).appendSuper("").toString());
        assertEquals("<null>", new ToStringBuilder(base).appendSuper("<null>").toString());
        
        assertEquals("hello", new ToStringBuilder(base).appendSuper("").append("a", "hello").toString());
        assertEquals("<null>,hello", new ToStringBuilder(base).appendSuper("<null>").append("a", "hello").toString());
        assertEquals("hello", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals("<null>", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals("3", new ToStringBuilder(base).append(i3).toString());
        assertEquals("<null>", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals("3", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals("3,4", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals("<Integer>", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals("[]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals("{}", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals("<size=0>", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals("{}", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Jane Q. Public";
        p.age = 47;
        p.smoker = false;
        assertEquals("Jane Q. Public,47,false", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testLong
    public void testLong() {
        assertEquals("3", new ToStringBuilder(base).append(3L).toString());
        assertEquals("3", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals("3,4", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals("{<null>,5,{3,6}}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{<null>,5,{3,6}}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals("{1,2,-3,4}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{1,2,-3,4}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.SimpleToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals("{{1,2},<null>,{5}}", new ToStringBuilder(base).append(array).toString());
        assertEquals("{{1,2},<null>,{5}}", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals("<null>", new ToStringBuilder(base).append(array).toString());
        assertEquals("<null>", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[%NULL%,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[%NULL%]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=%NULL%]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=%Integer%]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=%SIZE=0%]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testPerson
    public void testPerson() {
        Person p = new Person();
        p.name = "Suzy Queue";
        p.age = 19;
        p.smoker = false;
        String pBaseStr = "ToStringStyleTest.Person";
        assertEquals(pBaseStr + "[name=Suzy Queue,age=19,smoker=false]", new ToStringBuilder(p).append("name", p.name).append("age", p.age).append("smoker", p.smoker).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[%NULL%, 5, [3, 6]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[1, 2, -3, 4]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.StandardToStringStyleTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[[[1, 2], %NULL%, [5]]]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[%NULL%]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testConstructorEx1
    public void testConstructorEx1() {
        assertEquals("<null>", new ToStringBuilder(null).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testConstructorEx2
    public void testConstructorEx2() {
        assertEquals("<null>", new ToStringBuilder(null, null).toString());
        new ToStringBuilder(this.base, null).toString();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testConstructorEx3
    public void testConstructorEx3() {
        assertEquals("<null>", new ToStringBuilder(null, null, null).toString());
        new ToStringBuilder(this.base, null, null);
        new ToStringBuilder(this.base, ToStringStyle.DEFAULT_STYLE, null);
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testGetSetDefault
    public void testGetSetDefault() {
        try {
            ToStringBuilder.setDefaultStyle(ToStringStyle.NO_FIELD_NAMES_STYLE);
            assertSame(ToStringStyle.NO_FIELD_NAMES_STYLE, ToStringBuilder.getDefaultStyle());
        } finally {
            
            ToStringBuilder.setDefaultStyle(ToStringStyle.DEFAULT_STYLE);
        }
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSetDefaultEx
    public void testSetDefaultEx() {
        try {
            ToStringBuilder.setDefaultStyle(null);
            
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBlank
    public void testBlank() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionInteger
    public void testReflectionInteger() {
        assertEquals(baseStr + "[value=5]", ToStringBuilder.reflectionToString(base));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionCharacter
    public void testReflectionCharacter() {
        Character c = new Character('A');
        assertEquals(this.toBaseString(c) + "[value=A]", ToStringBuilder.reflectionToString(c));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionBoolean
    public void testReflectionBoolean() {
        Boolean b;
        b = Boolean.TRUE;
        assertEquals(this.toBaseString(b) + "[value=true]", ToStringBuilder.reflectionToString(b));
        b = Boolean.FALSE;
        assertEquals(this.toBaseString(b) + "[value=false]", ToStringBuilder.reflectionToString(b));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionObjectArray
    public void testReflectionObjectArray() {
        Object[] array = new Object[] { null, base, new int[] { 3, 6 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionLongArray
    public void testReflectionLongArray() {
        long[] array = new long[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionIntArray
    public void testReflectionIntArray() {
        int[] array = new int[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionShortArray
    public void testReflectionShortArray() {
        short[] array = new short[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionyteArray
    public void testReflectionyteArray() {
        byte[] array = new byte[] { 1, 2, -3, 4 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1,2,-3,4}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionCharArray
    public void testReflectionCharArray() {
        char[] array = new char[] { 'A', '2', '_', 'D' };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{A,2,_,D}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionDoubleArray
    public void testReflectionDoubleArray() {
        double[] array = new double[] { 1.0, 2.9876, -3.00001, 4.3 };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionFloatArray
    public void testReflectionFloatArray() {
        float[] array = new float[] { 1.0f, 2.9876f, -3.00001f, 4.3f };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionBooleanArray
    public void testReflectionBooleanArray() {
        boolean[] array = new boolean[] { true, false, false };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{true,false,false}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionFloatArrayArray
    public void testReflectionFloatArrayArray() {
        float[][] array = new float[][] { { 1.0f, 2.29686f }, null, { Float.NaN } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionLongArrayArray
    public void testReflectionLongArrayArray() {
        long[][] array = new long[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionIntArrayArray
    public void testReflectionIntArrayArray() {
        int[][] array = new int[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionhortArrayArray
    public void testReflectionhortArrayArray() {
        short[][] array = new short[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionByteArrayArray
    public void testReflectionByteArrayArray() {
        byte[][] array = new byte[][] { { 1, 2 }, null, { 5 } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionCharArrayArray
    public void testReflectionCharArrayArray() {
        char[][] array = new char[][] { { 'A', 'B' }, null, { 'p' } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{A,B},<null>,{p}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionDoubleArrayArray
    public void testReflectionDoubleArrayArray() {
        double[][] array = new double[][] { { 1.0, 2.29686 }, null, { Double.NaN } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionBooleanArrayArray
    public void testReflectionBooleanArrayArray() {
        boolean[][] array = new boolean[][] { { true, false }, null, { false } };
        String baseStr = this.toBaseString(array);
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", ToStringBuilder.reflectionToString(array));
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", ToStringBuilder.reflectionToString(array));
        array = null;
        assertReflectionArray("<null>", array);
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionHierarchyArrayList
    public void testReflectionHierarchyArrayList() {}

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionHierarchy
    public void testReflectionHierarchy() {
        ReflectionTestFixtureA baseA = new ReflectionTestFixtureA();
        String baseStr = this.toBaseString(baseA);
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false));
        assertEquals(baseStr + "[a=a,transientA=t]", ToStringBuilder.reflectionToString(baseA, null, true));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, null));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, Object.class));
        assertEquals(baseStr + "[a=a]", ToStringBuilder.reflectionToString(baseA, null, false, ReflectionTestFixtureA.class));
        
        ReflectionTestFixtureB baseB = new ReflectionTestFixtureB();
        baseStr = this.toBaseString(baseB);
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false));
        assertEquals(baseStr + "[b=b,transientB=t,a=a,transientA=t]", ToStringBuilder.reflectionToString(baseB, null, true));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, null));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, Object.class));
        assertEquals(baseStr + "[b=b,a=a]", ToStringBuilder.reflectionToString(baseB, null, false, ReflectionTestFixtureA.class));
        assertEquals(baseStr + "[b=b]", ToStringBuilder.reflectionToString(baseB, null, false, ReflectionTestFixtureB.class));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testInnerClassReflection
    public void testInnerClassReflection() {
        Outer outer = new Outer();
        assertEquals(toBaseString(outer) + "[inner=" + toBaseString(outer.inner) + "[]]", outer.toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayCycle
    public void testReflectionArrayCycle() throws Exception {
        Object[] objects = new Object[1];
        objects[0] = objects;
        assertEquals(
            this.toBaseString(objects) + "[{" + this.toBaseString(objects) + "}]",
            ToStringBuilder.reflectionToString(objects));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayCycleLevel2
    public void testReflectionArrayCycleLevel2() throws Exception {
        Object[] objects = new Object[1];
        Object[] objectsLevel2 = new Object[1];
        objects[0] = objectsLevel2;
        objectsLevel2[0] = objects;
        assertEquals(
            this.toBaseString(objects) + "[{{" + this.toBaseString(objects) + "}}]",
            ToStringBuilder.reflectionToString(objects));
        assertEquals(
            this.toBaseString(objectsLevel2) + "[{{" + this.toBaseString(objectsLevel2) + "}}]",
            ToStringBuilder.reflectionToString(objectsLevel2));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayArrayCycle
    public void testReflectionArrayArrayCycle() throws Exception {
        Object[][] objects = new Object[2][2];
        objects[0][0] = objects;
        objects[0][1] = objects;
        objects[1][0] = objects;
        objects[1][1] = objects;
        String basicToString = this.toBaseString(objects);
        assertEquals(
            basicToString
                + "[{{"
                + basicToString
                + ","
                + basicToString
                + "},{"
                + basicToString
                + ","
                + basicToString
                + "}}]",
            ToStringBuilder.reflectionToString(objects));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSimpleReflectionObjectCycle
    public void testSimpleReflectionObjectCycle() throws Exception {
        SimpleReflectionTestFixture simple = new SimpleReflectionTestFixture();
        simple.o = simple;
        assertTrue(ToStringStyle.getRegistry().isEmpty());
        assertEquals(this.toBaseString(simple) + "[o=" + this.toBaseString(simple) + "]", simple.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSelfInstanceVarReflectionObjectCycle
    public void testSelfInstanceVarReflectionObjectCycle() throws Exception {
        SelfInstanceVarReflectionTestFixture test = new SelfInstanceVarReflectionTestFixture();
        assertTrue(ToStringStyle.getRegistry().isEmpty());
        assertEquals(this.toBaseString(test) + "[typeIsSelf=" + this.toBaseString(test) + "]", test.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSelfInstanceTwoVarsReflectionObjectCycle
    public void testSelfInstanceTwoVarsReflectionObjectCycle() throws Exception {
        SelfInstanceTwoVarsReflectionTestFixture test = new SelfInstanceTwoVarsReflectionTestFixture();
        assertTrue(ToStringStyle.getRegistry().isEmpty());
        assertEquals(this.toBaseString(test) + "[typeIsSelf=" + this.toBaseString(test) + ",otherType=" + test.getOtherType().toString() + "]", test.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionObjectCycle
    public void testReflectionObjectCycle() throws Exception {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;
        assertEquals(
            this.toBaseString(a) + "[b=" + this.toBaseString(b) + "[a=" + this.toBaseString(a) + "]]",
            a.toString());
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionArrayAndObjectCycle
    public void testReflectionArrayAndObjectCycle() throws Exception {
        Object[] objects = new Object[1];
        SimpleReflectionTestFixture simple = new SimpleReflectionTestFixture(objects);
        objects[0] = simple;
        assertEquals(
            this.toBaseString(objects)
                + "[{"
                + this.toBaseString(simple)
                + "[o="
                + this.toBaseString(objects)
                + "]"
                + "}]",
            ToStringBuilder.reflectionToString(objects));
        assertEquals(
            this.toBaseString(simple)
                + "[o={"
                + this.toBaseString(simple)
                + "}]",
            ToStringBuilder.reflectionToString(simple));
        this.validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testAppendSuper
    public void testAppendSuper() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendSuper("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendSuper("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendSuper(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testAppendToString
    public void testAppendToString() {
        assertEquals(baseStr + "[]", new ToStringBuilder(base).appendToString("Integer@8888[]").toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).appendToString("Integer@8888[<null>]").toString());
        
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendToString("Integer@8888[]").append("a", "hello").toString());
        assertEquals(baseStr + "[<null>,a=hello]", new ToStringBuilder(base).appendToString("Integer@8888[<null>]").append("a", "hello").toString());
        assertEquals(baseStr + "[a=hello]", new ToStringBuilder(base).appendToString(null).append("a", "hello").toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testObject
    public void testObject() {
        Integer i3 = new Integer(3);
        Integer i4 = new Integer(4);
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) null).toString());
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(i3).toString());
        assertEquals(baseStr + "[a=<null>]", new ToStringBuilder(base).append("a", (Object) null).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", i3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", i3).append("b", i4).toString());
        assertEquals(baseStr + "[a=<Integer>]", new ToStringBuilder(base).append("a", i3, false).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new ArrayList(), false).toString());
        assertEquals(baseStr + "[a=[]]", new ToStringBuilder(base).append("a", new ArrayList(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", new HashMap(), false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", new HashMap(), true).toString());
        assertEquals(baseStr + "[a=<size=0>]", new ToStringBuilder(base).append("a", (Object) new String[0], false).toString());
        assertEquals(baseStr + "[a={}]", new ToStringBuilder(base).append("a", (Object) new String[0], true).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testLong
    public void testLong() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append(3L).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", 3L).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", 3L).append("b", 4L).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testInt
    public void testInt() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append((int) 3).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", (int) 3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", (int) 3).append("b", (int) 4).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testShort
    public void testShort() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append((short) 3).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", (short) 3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", (short) 3).append("b", (short) 4).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testChar
    public void testChar() {
        assertEquals(baseStr + "[A]", new ToStringBuilder(base).append((char) 65).toString());
        assertEquals(baseStr + "[a=A]", new ToStringBuilder(base).append("a", (char) 65).toString());
        assertEquals(baseStr + "[a=A,b=B]", new ToStringBuilder(base).append("a", (char) 65).append("b", (char) 66).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testByte
    public void testByte() {
        assertEquals(baseStr + "[3]", new ToStringBuilder(base).append((byte) 3).toString());
        assertEquals(baseStr + "[a=3]", new ToStringBuilder(base).append("a", (byte) 3).toString());
        assertEquals(baseStr + "[a=3,b=4]", new ToStringBuilder(base).append("a", (byte) 3).append("b", (byte) 4).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testDouble
    public void testDouble() {
        assertEquals(baseStr + "[3.2]", new ToStringBuilder(base).append((double) 3.2).toString());
        assertEquals(baseStr + "[a=3.2]", new ToStringBuilder(base).append("a", (double) 3.2).toString());
        assertEquals(baseStr + "[a=3.2,b=4.3]", new ToStringBuilder(base).append("a", (double) 3.2).append("b", (double) 4.3).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testFloat
    public void testFloat() {
        assertEquals(baseStr + "[3.2]", new ToStringBuilder(base).append((float) 3.2).toString());
        assertEquals(baseStr + "[a=3.2]", new ToStringBuilder(base).append("a", (float) 3.2).toString());
        assertEquals(baseStr + "[a=3.2,b=4.3]", new ToStringBuilder(base).append("a", (float) 3.2).append("b", (float) 4.3).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBoolean
    public void testBoolean() {
        assertEquals(baseStr + "[true]", new ToStringBuilder(base).append(true).toString());
        assertEquals(baseStr + "[a=true]", new ToStringBuilder(base).append("a", true).toString());
        assertEquals(baseStr + "[a=true,b=false]", new ToStringBuilder(base).append("a", true).append("b", false).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testObjectArray
    public void testObjectArray() {
        Object[] array = new Object[] {null, base, new int[] {3, 6}};
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{<null>,5,{3,6}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testLongArray
    public void testLongArray() {
        long[] array = new long[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testIntArray
    public void testIntArray() {
        int[] array = new int[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testShortArray
    public void testShortArray() {
        short[] array = new short[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testByteArray
    public void testByteArray() {
        byte[] array = new byte[] {1, 2, -3, 4};
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1,2,-3,4}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testCharArray
    public void testCharArray() {
        char[] array = new char[] {'A', '2', '_', 'D'};
        assertEquals(baseStr + "[{A,2,_,D}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{A,2,_,D}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testDoubleArray
    public void testDoubleArray() {
        double[] array = new double[] {1.0, 2.9876, -3.00001, 4.3};
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testFloatArray
    public void testFloatArray() {
        float[] array = new float[] {1.0f, 2.9876f, -3.00001f, 4.3f};
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{1.0,2.9876,-3.00001,4.3}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBooleanArray
    public void testBooleanArray() {
        boolean[] array = new boolean[] {true, false, false};
        assertEquals(baseStr + "[{true,false,false}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{true,false,false}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testLongArrayArray
    public void testLongArrayArray() {
        long[][] array = new long[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testIntArrayArray
    public void testIntArrayArray() {
        int[][] array = new int[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testShortArrayArray
    public void testShortArrayArray() {
        short[][] array = new short[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testByteArrayArray
    public void testByteArrayArray() {
        byte[][] array = new byte[][] {{1, 2}, null, {5}};
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1,2},<null>,{5}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testCharArrayArray
    public void testCharArrayArray() {
        char[][] array = new char[][] {{'A', 'B'}, null, {'p'}};
        assertEquals(baseStr + "[{{A,B},<null>,{p}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{A,B},<null>,{p}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testDoubleArrayArray
    public void testDoubleArrayArray() {
        double[][] array = new double[][] {{1.0, 2.29686}, null, {Double.NaN}};
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testFloatArrayArray
    public void testFloatArrayArray() {
        float[][] array = new float[][] {{1.0f, 2.29686f}, null, {Float.NaN}};
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{1.0,2.29686},<null>,{NaN}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testBooleanArrayArray
    public void testBooleanArrayArray() {
        boolean[][] array = new boolean[][] {{true, false}, null, {false}};
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[{{true,false},<null>,{false}}]", new ToStringBuilder(base).append((Object) array).toString());
        array = null;
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append(array).toString());
        assertEquals(baseStr + "[<null>]", new ToStringBuilder(base).append((Object) array).toString());
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testObjectCycle
    public void testObjectCycle() {
        ObjectCycle a = new ObjectCycle();
        ObjectCycle b = new ObjectCycle();
        a.obj = b;
        b.obj = a;
       
        String expected = toBaseString(a) + "[" + toBaseString(b) + "[" + toBaseString(a) + "]]";
        assertEquals(expected, a.toString());
        validateEmptyToStringStyleRegistry();
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testSimpleReflectionStatics
    public void testSimpleReflectionStatics() {
        SimpleReflectionStaticFieldsFixture instance1 = new SimpleReflectionStaticFieldsFixture();
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            ReflectionToStringBuilder.toString(instance1, null, true, true, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionStatics
    public void testReflectionStatics() {
        ReflectionStaticFieldsFixture instance1 = new ReflectionStaticFieldsFixture();
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,instanceString=instanceString,instanceInt=67890]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, ReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,staticTransientString=staticTransientString,staticTransientInt=54321,instanceString=instanceString,instanceInt=67890,transientString=transientString,transientInt=98765]",
            ReflectionToStringBuilder.toString(instance1, null, true, true, ReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,instanceString=instanceString,instanceInt=67890]",
            this.toStringWithStatics(instance1, null, ReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString=staticString,staticInt=12345,instanceString=instanceString,instanceInt=67890]",
            this.toStringWithStatics(instance1, null, ReflectionStaticFieldsFixture.class));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testInheritedReflectionStatics
    public void testInheritedReflectionStatics() {
        InheritedReflectionStaticFieldsFixture instance1 = new InheritedReflectionStaticFieldsFixture();
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, InheritedReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890,staticString=staticString,staticInt=12345]",
            ReflectionToStringBuilder.toString(instance1, null, false, true, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890,staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
        assertEquals(
            this.toBaseString(instance1) + "[staticString2=staticString2,staticInt2=67890,staticString=staticString,staticInt=12345]",
            this.toStringWithStatics(instance1, null, SimpleReflectionStaticFieldsFixture.class));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testReflectionNull
    public void testReflectionNull() {
        assertEquals("<null>", ReflectionToStringBuilder.toString(null));
    }

// org.apache.commons.lang.builder.ToStringBuilderTest::testAppendToStringUsingMultiLineStyle
    public void testAppendToStringUsingMultiLineStyle() {
        MultiLineTestObject obj = new MultiLineTestObject();
        ToStringBuilder testBuilder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                                          .appendToString(obj.toString());
        assertEquals(testBuilder.toString().indexOf("testInt=31337"), -1);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new ExceptionUtils());
        Constructor[] cons = ExceptionUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(ExceptionUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ExceptionUtils.class.getModifiers()));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testCauseMethodNameOps
    public void testCauseMethodNameOps() {
        this.testCauseMethodNameOps(null);
        this.testCauseMethodNameOps("");
        this.testCauseMethodNameOps(" ");
        this.testCauseMethodNameOps("\t\r\n\t");
        this.testCauseMethodNameOps("testMethodName");
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetCause_Throwable
    public void testGetCause_Throwable() {
        assertSame(null, ExceptionUtils.getCause(null));
        assertSame(null, ExceptionUtils.getCause(withoutCause));
        assertSame(withoutCause, ExceptionUtils.getCause(nested));
        assertSame(nested, ExceptionUtils.getCause(withCause));
        assertSame(null, ExceptionUtils.getCause(jdkNoCause));
        assertSame(selfCause, ExceptionUtils.getCause(selfCause));
        assertSame(cyclicCause.getCause(), ExceptionUtils.getCause(cyclicCause));
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), ExceptionUtils.getCause(cyclicCause.getCause()));
        assertSame(cyclicCause.getCause(), ExceptionUtils.getCause(((ExceptionWithCause) cyclicCause.getCause()).getCause()));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetCause_ThrowableArray
    public void testGetCause_ThrowableArray() {
        assertSame(null, ExceptionUtils.getCause(null, null));
        assertSame(null, ExceptionUtils.getCause(null, new String[0]));

        
        assertSame(nested, ExceptionUtils.getCause(withCause, null));  
        assertSame(null, ExceptionUtils.getCause(withCause, new String[0]));
        assertSame(null, ExceptionUtils.getCause(withCause, new String[] {null}));
        assertSame(nested, ExceptionUtils.getCause(withCause, new String[] {"getCause"}));
        
        
        assertSame(null, ExceptionUtils.getCause(withoutCause, null));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[0]));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[] {null}));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[] {"getCause"}));
        assertSame(null, ExceptionUtils.getCause(withoutCause, new String[] {"getTargetException"}));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetRootCause_Throwable
    public void testGetRootCause_Throwable() {
        assertSame(null, ExceptionUtils.getRootCause(null));
        assertSame(null, ExceptionUtils.getRootCause(withoutCause));
        assertSame(withoutCause, ExceptionUtils.getRootCause(nested));
        assertSame(withoutCause, ExceptionUtils.getRootCause(withCause));
        assertSame(null, ExceptionUtils.getRootCause(jdkNoCause));
        assertSame(null, ExceptionUtils.getRootCause(selfCause));
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), ExceptionUtils.getRootCause(cyclicCause));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testSetCause
    public void testSetCause() {
        Exception cause = new ExceptionWithoutCause();
        assertEquals(true, ExceptionUtils.setCause(new ExceptionWithCause(null), cause));
        if (SystemUtils.isJavaVersionAtLeast(140)) {
            assertEquals(true, ExceptionUtils.setCause(new ExceptionWithoutCause(), cause));
        }
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testSetCauseToNull
    public void testSetCauseToNull() {
        Exception ex = new ExceptionWithCause(new IOException());
        assertEquals(true, ExceptionUtils.setCause(ex, new IllegalStateException()));
        assertNotNull(ExceptionUtils.getCause(ex));
        assertEquals(true, ExceptionUtils.setCause(ex, null));
        assertNull(ExceptionUtils.getCause(ex));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testIsThrowableNested
    public void testIsThrowableNested() {}

// org.apache.commons.lang.exception.ExceptionUtilsTest::testIsNestedThrowable_Throwable
    public void testIsNestedThrowable_Throwable() {}

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableCount_Throwable
    public void testGetThrowableCount_Throwable() {
        assertEquals(0, ExceptionUtils.getThrowableCount(null));
        assertEquals(1, ExceptionUtils.getThrowableCount(withoutCause));
        assertEquals(2, ExceptionUtils.getThrowableCount(nested));
        assertEquals(3, ExceptionUtils.getThrowableCount(withCause));
        assertEquals(1, ExceptionUtils.getThrowableCount(jdkNoCause));
        assertEquals(1, ExceptionUtils.getThrowableCount(selfCause));
        assertEquals(3, ExceptionUtils.getThrowableCount(cyclicCause));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowables_Throwable_null
    public void testGetThrowables_Throwable_null() {
        assertEquals(0, ExceptionUtils.getThrowables(null).length);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowables_Throwable_withoutCause
    public void testGetThrowables_Throwable_withoutCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(withoutCause);
        assertEquals(1, throwables.length);
        assertSame(withoutCause, throwables[0]);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowables_Throwable_nested
    public void testGetThrowables_Throwable_nested() {
        Throwable[] throwables = ExceptionUtils.getThrowables(nested);
        assertEquals(2, throwables.length);
        assertSame(nested, throwables[0]);
        assertSame(withoutCause, throwables[1]);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowables_Throwable_withCause
    public void testGetThrowables_Throwable_withCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(withCause);
        assertEquals(3, throwables.length);
        assertSame(withCause, throwables[0]);
        assertSame(nested, throwables[1]);
        assertSame(withoutCause, throwables[2]);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowables_Throwable_jdkNoCause
    public void testGetThrowables_Throwable_jdkNoCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(jdkNoCause);
        assertEquals(1, throwables.length);
        assertSame(jdkNoCause, throwables[0]);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowables_Throwable_selfCause
    public void testGetThrowables_Throwable_selfCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(selfCause);
        assertEquals(1, throwables.length);
        assertSame(selfCause, throwables[0]);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowables_Throwable_recursiveCause
    public void testGetThrowables_Throwable_recursiveCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(cyclicCause);
        assertEquals(3, throwables.length);
        assertSame(cyclicCause, throwables[0]);
        assertSame(cyclicCause.getCause(), throwables[1]);
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), throwables[2]);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_null
    public void testGetThrowableList_Throwable_null() {
        List throwables = ExceptionUtils.getThrowableList(null);
        assertEquals(0, throwables.size());
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_withoutCause
    public void testGetThrowableList_Throwable_withoutCause() {
        List throwables = ExceptionUtils.getThrowableList(withoutCause);
        assertEquals(1, throwables.size());
        assertSame(withoutCause, throwables.get(0));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_nested
    public void testGetThrowableList_Throwable_nested() {
        List throwables = ExceptionUtils.getThrowableList(nested);
        assertEquals(2, throwables.size());
        assertSame(nested, throwables.get(0));
        assertSame(withoutCause, throwables.get(1));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_withCause
    public void testGetThrowableList_Throwable_withCause() {
        List throwables = ExceptionUtils.getThrowableList(withCause);
        assertEquals(3, throwables.size());
        assertSame(withCause, throwables.get(0));
        assertSame(nested, throwables.get(1));
        assertSame(withoutCause, throwables.get(2));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_jdkNoCause
    public void testGetThrowableList_Throwable_jdkNoCause() {
        List throwables = ExceptionUtils.getThrowableList(jdkNoCause);
        assertEquals(1, throwables.size());
        assertSame(jdkNoCause, throwables.get(0));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_selfCause
    public void testGetThrowableList_Throwable_selfCause() {
        List throwables = ExceptionUtils.getThrowableList(selfCause);
        assertEquals(1, throwables.size());
        assertSame(selfCause, throwables.get(0));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_recursiveCause
    public void testGetThrowableList_Throwable_recursiveCause() {
        List throwables = ExceptionUtils.getThrowableList(cyclicCause);
        assertEquals(3, throwables.size());
        assertSame(cyclicCause, throwables.get(0));
        assertSame(cyclicCause.getCause(), throwables.get(1));
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), throwables.get(2));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testIndexOf_ThrowableClass
    public void testIndexOf_ThrowableClass() {
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, NestableException.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithCause.class));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, NestableException.class));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithCause.class));
        assertEquals(0, ExceptionUtils.indexOfThrowable(nested, NestableException.class));
        assertEquals(1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class));
        assertEquals(1, ExceptionUtils.indexOfThrowable(withCause, NestableException.class));
        assertEquals(2, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, Exception.class));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testIndexOf_ThrowableClassInt
    public void testIndexOf_ThrowableClassInt() {
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(null, NestableException.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withoutCause, NestableException.class, 0));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withoutCause, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithCause.class, 0));
        assertEquals(0, ExceptionUtils.indexOfThrowable(nested, NestableException.class, 0));
        assertEquals(1, ExceptionUtils.indexOfThrowable(nested, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 0));
        assertEquals(1, ExceptionUtils.indexOfThrowable(withCause, NestableException.class, 0));
        assertEquals(2, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithoutCause.class, 0));

        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, -1));
        assertEquals(0, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 1));
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, ExceptionWithCause.class, 9));
        
        assertEquals(-1, ExceptionUtils.indexOfThrowable(withCause, Exception.class, 0));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testIndexOfType_ThrowableClass
    public void testIndexOfType_ThrowableClass() {
        assertEquals(-1, ExceptionUtils.indexOfType(null, null));
        assertEquals(-1, ExceptionUtils.indexOfType(null, NestableException.class));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, ExceptionWithCause.class));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, NestableException.class));
        assertEquals(0, ExceptionUtils.indexOfType(withoutCause, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfType(nested, null));
        assertEquals(-1, ExceptionUtils.indexOfType(nested, ExceptionWithCause.class));
        assertEquals(0, ExceptionUtils.indexOfType(nested, NestableException.class));
        assertEquals(1, ExceptionUtils.indexOfType(nested, ExceptionWithoutCause.class));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class));
        assertEquals(1, ExceptionUtils.indexOfType(withCause, NestableException.class));
        assertEquals(2, ExceptionUtils.indexOfType(withCause, ExceptionWithoutCause.class));
        
        assertEquals(0, ExceptionUtils.indexOfType(withCause, Exception.class));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testIndexOfType_ThrowableClassInt
    public void testIndexOfType_ThrowableClassInt() {
        assertEquals(-1, ExceptionUtils.indexOfType(null, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(null, NestableException.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, null));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(withoutCause, NestableException.class, 0));
        assertEquals(0, ExceptionUtils.indexOfType(withoutCause, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfType(nested, null, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(nested, ExceptionWithCause.class, 0));
        assertEquals(0, ExceptionUtils.indexOfType(nested, NestableException.class, 0));
        assertEquals(1, ExceptionUtils.indexOfType(nested, ExceptionWithoutCause.class, 0));
        
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, null));
        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 0));
        assertEquals(1, ExceptionUtils.indexOfType(withCause, NestableException.class, 0));
        assertEquals(2, ExceptionUtils.indexOfType(withCause, ExceptionWithoutCause.class, 0));

        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, -1));
        assertEquals(0, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 0));
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 1));
        assertEquals(-1, ExceptionUtils.indexOfType(withCause, ExceptionWithCause.class, 9));
        
        assertEquals(0, ExceptionUtils.indexOfType(withCause, Exception.class, 0));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_Throwable
    public void testPrintRootCauseStackTrace_Throwable() throws Exception {
        ExceptionUtils.printRootCauseStackTrace(null);
        
        
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_ThrowableStream
    public void testPrintRootCauseStackTrace_ThrowableStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        ExceptionUtils.printRootCauseStackTrace(null, (PrintStream) null);
        ExceptionUtils.printRootCauseStackTrace(null, new PrintStream(out));
        assertEquals(0, out.toString().length());
        
        out = new ByteArrayOutputStream(1024);
        try {
            ExceptionUtils.printRootCauseStackTrace(withCause, (PrintStream) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        out = new ByteArrayOutputStream(1024);
        Throwable withCause = createExceptionWithCause();
        ExceptionUtils.printRootCauseStackTrace(withCause, new PrintStream(out));
        String stackTrace = out.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) != -1);
        
        out = new ByteArrayOutputStream(1024);
        ExceptionUtils.printRootCauseStackTrace(withoutCause, new PrintStream(out));
        stackTrace = out.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) == -1);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_ThrowableWriter
    public void testPrintRootCauseStackTrace_ThrowableWriter() throws Exception {
        StringWriter writer = new StringWriter(1024);
        ExceptionUtils.printRootCauseStackTrace(null, (PrintWriter) null);
        ExceptionUtils.printRootCauseStackTrace(null, new PrintWriter(writer));
        assertEquals(0, writer.getBuffer().length());
        
        writer = new StringWriter(1024);
        try {
            ExceptionUtils.printRootCauseStackTrace(withCause, (PrintWriter) null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
        
        writer = new StringWriter(1024);
        Throwable withCause = createExceptionWithCause();
        ExceptionUtils.printRootCauseStackTrace(withCause, new PrintWriter(writer));
        String stackTrace = writer.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) != -1);
        
        writer = new StringWriter(1024);
        ExceptionUtils.printRootCauseStackTrace(withoutCause, new PrintWriter(writer));
        stackTrace = writer.toString();
        assertTrue(stackTrace.indexOf(ExceptionUtils.WRAPPED_MARKER) == -1);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testGetRootCauseStackTrace_Throwable
    public void testGetRootCauseStackTrace_Throwable() throws Exception {
        assertEquals(0, ExceptionUtils.getRootCauseStackTrace(null).length);
        
        Throwable withCause = createExceptionWithCause();
        String[] stackTrace = ExceptionUtils.getRootCauseStackTrace(withCause);
        boolean match = false;
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].startsWith(ExceptionUtils.WRAPPED_MARKER)) {
                match = true;
                break;
            }
        }
        assertEquals(true, match);
        
        stackTrace = ExceptionUtils.getRootCauseStackTrace(withoutCause);
        match = false;
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].startsWith(ExceptionUtils.WRAPPED_MARKER)) {
                match = true;
                break;
            }
        }
        assertEquals(false, match);
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::testRemoveCommonFrames_ListList
    public void testRemoveCommonFrames_ListList() throws Exception {
        try {
            ExceptionUtils.removeCommonFrames(null, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::test_getMessage_Throwable
    public void test_getMessage_Throwable() {
        Throwable th = null;
        assertEquals("", ExceptionUtils.getMessage(th));
        
        th = new IllegalArgumentException("Base");
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getMessage(th));
        
        th = new ExceptionWithCause("Wrapper", th);
        assertEquals("ExceptionUtilsTest.ExceptionWithCause: Wrapper", ExceptionUtils.getMessage(th));
    }

// org.apache.commons.lang.exception.ExceptionUtilsTest::test_getRootCauseMessage_Throwable
    public void test_getRootCauseMessage_Throwable() {
        Throwable th = null;
        assertEquals("", ExceptionUtils.getRootCauseMessage(th));
        
        th = new IllegalArgumentException("Base");
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getRootCauseMessage(th));
        
        th = new ExceptionWithCause("Wrapper", th);
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getRootCauseMessage(th));
    }

// org.apache.commons.lang.reflect.ConstructorUtilsTest::testConstructor
    public void testConstructor() throws Exception {
        assertNotNull(MethodUtils.class.newInstance());
    }

// org.apache.commons.lang.reflect.ConstructorUtilsTest::testInvokeConstructor
    public void testInvokeConstructor() throws Exception {
        assertEquals("()", ConstructorUtils.invokeConstructor(TestBean.class,
                ArrayUtils.EMPTY_CLASS_ARRAY).toString());
        assertEquals("()", ConstructorUtils.invokeConstructor(TestBean.class,
                (Class[]) null).toString());
        assertEquals("(String)", ConstructorUtils.invokeConstructor(
                TestBean.class, "").toString());
        assertEquals("(Object)", ConstructorUtils.invokeConstructor(
                TestBean.class, new Object()).toString());
        assertEquals("(Object)", ConstructorUtils.invokeConstructor(
                TestBean.class, Boolean.TRUE).toString());
        assertEquals("(Integer)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.INTEGER_ONE).toString());
        assertEquals("(int)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.BYTE_ONE).toString());
        assertEquals("(double)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.LONG_ONE).toString());
        assertEquals("(double)", ConstructorUtils.invokeConstructor(
                TestBean.class, NumberUtils.DOUBLE_ONE).toString());
    }

// org.apache.commons.lang.reflect.ConstructorUtilsTest::testInvokeExactConstructor
    public void testInvokeExactConstructor() throws Exception {
        assertEquals("()", ConstructorUtils.invokeExactConstructor(
                TestBean.class, ArrayUtils.EMPTY_CLASS_ARRAY).toString());
        assertEquals("()", ConstructorUtils.invokeExactConstructor(
                TestBean.class, (Class[]) null).toString());
        assertEquals("(String)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, "").toString());
        assertEquals("(Object)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, new Object()).toString());
        assertEquals("(Integer)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, NumberUtils.INTEGER_ONE).toString());
        assertEquals("(double)", ConstructorUtils.invokeExactConstructor(
                TestBean.class, new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }).toString());

        try {
            ConstructorUtils.invokeExactConstructor(TestBean.class,
                    NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            ConstructorUtils.invokeExactConstructor(TestBean.class,
                    NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            ConstructorUtils.invokeExactConstructor(TestBean.class,
                    Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
    }

// org.apache.commons.lang.reflect.ConstructorUtilsTest::testGetAccessibleConstructor
    public void testGetAccessibleConstructor() throws Exception {
        assertNotNull(ConstructorUtils.getAccessibleConstructor(Object.class
                .getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY)));
        assertNull(ConstructorUtils.getAccessibleConstructor(PrivateClass.class
                .getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY)));
    }

// org.apache.commons.lang.reflect.ConstructorUtilsTest::testGetAccessibleConstructorFromDescription
    public void testGetAccessibleConstructorFromDescription() throws Exception {
        assertNotNull(ConstructorUtils.getAccessibleConstructor(Object.class,
                ArrayUtils.EMPTY_CLASS_ARRAY));
        assertNull(ConstructorUtils.getAccessibleConstructor(
                PrivateClass.class, ArrayUtils.EMPTY_CLASS_ARRAY));
    }

// org.apache.commons.lang.reflect.ConstructorUtilsTest::testGetMatchingAccessibleMethod
    public void testGetMatchingAccessibleMethod() throws Exception {
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class, null,
                ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(String.class), singletonArray(String.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Object.class), singletonArray(Object.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Boolean.class), singletonArray(Object.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Byte.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Byte.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Short.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Short.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Character.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Character.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Integer.class), singletonArray(Integer.class));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Integer.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Long.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Long.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Float.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Float.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Double.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleConstructorParameterTypes(TestBean.class,
                singletonArray(Double.TYPE), singletonArray(Double.TYPE));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testGetField
    public void testGetField() {
        assertEquals(Foo.class, FieldUtils.getField(PublicChild.class, "VALUE").getDeclaringClass());
        assertEquals(parentClass, FieldUtils.getField(PublicChild.class, "s").getDeclaringClass());
        assertNull(FieldUtils.getField(PublicChild.class, "b"));
        assertNull(FieldUtils.getField(PublicChild.class, "i"));
        assertNull(FieldUtils.getField(PublicChild.class, "d"));
        assertEquals(Foo.class, FieldUtils.getField(PubliclyShadowedChild.class, "VALUE").getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "s")
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "b")
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "i")
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "d")
                .getDeclaringClass());
        assertEquals(Foo.class, FieldUtils.getField(PrivatelyShadowedChild.class, "VALUE").getDeclaringClass());
        assertEquals(parentClass, FieldUtils.getField(PrivatelyShadowedChild.class, "s").getDeclaringClass());
        assertNull(FieldUtils.getField(PrivatelyShadowedChild.class, "b"));
        assertNull(FieldUtils.getField(PrivatelyShadowedChild.class, "i"));
        assertNull(FieldUtils.getField(PrivatelyShadowedChild.class, "d"));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testGetFieldForceAccess
    public void testGetFieldForceAccess() {
        assertEquals(PublicChild.class, FieldUtils.getField(PublicChild.class, "VALUE", true).getDeclaringClass());
        assertEquals(parentClass, FieldUtils.getField(PublicChild.class, "s", true).getDeclaringClass());
        assertEquals(parentClass, FieldUtils.getField(PublicChild.class, "b", true).getDeclaringClass());
        assertEquals(parentClass, FieldUtils.getField(PublicChild.class, "i", true).getDeclaringClass());
        assertEquals(parentClass, FieldUtils.getField(PublicChild.class, "d", true).getDeclaringClass());
        assertEquals(Foo.class, FieldUtils.getField(PubliclyShadowedChild.class, "VALUE", true).getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "s", true)
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "b", true)
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "i", true)
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getField(PubliclyShadowedChild.class, "d", true)
                .getDeclaringClass());
        assertEquals(Foo.class, FieldUtils.getField(PrivatelyShadowedChild.class, "VALUE", true).getDeclaringClass());
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getField(PrivatelyShadowedChild.class, "s", true)
                .getDeclaringClass());
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getField(PrivatelyShadowedChild.class, "b", true)
                .getDeclaringClass());
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getField(PrivatelyShadowedChild.class, "i", true)
                .getDeclaringClass());
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getField(PrivatelyShadowedChild.class, "d", true)
                .getDeclaringClass());
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testGetDeclaredField
    public void testGetDeclaredField() {
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "VALUE"));
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "s"));
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "b"));
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "i"));
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "d"));
        assertNull(FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "VALUE"));
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "s")
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "b")
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "i")
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "d")
                .getDeclaringClass());
        assertNull(FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "VALUE"));
        assertNull(FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "s"));
        assertNull(FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "b"));
        assertNull(FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "i"));
        assertNull(FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "d"));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testGetDeclaredFieldForceAccess
    public void testGetDeclaredFieldForceAccess() {
        assertEquals(PublicChild.class, FieldUtils.getDeclaredField(PublicChild.class, "VALUE", true)
                .getDeclaringClass());
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "s", true));
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "b", true));
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "i", true));
        assertNull(FieldUtils.getDeclaredField(PublicChild.class, "d", true));
        assertNull(FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "VALUE", true));
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "s", true)
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "b", true)
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "i", true)
                .getDeclaringClass());
        assertEquals(PubliclyShadowedChild.class, FieldUtils.getDeclaredField(PubliclyShadowedChild.class, "d", true)
                .getDeclaringClass());
        assertNull(FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "VALUE", true));
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "s", true)
                .getDeclaringClass());
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "b", true)
                .getDeclaringClass());
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "i", true)
                .getDeclaringClass());
        assertEquals(PrivatelyShadowedChild.class, FieldUtils.getDeclaredField(PrivatelyShadowedChild.class, "d", true)
                .getDeclaringClass());
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadStaticField
    public void testReadStaticField() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(FieldUtils.getField(Foo.class, "VALUE")));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadStaticFieldForceAccess
    public void testReadStaticFieldForceAccess() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(FieldUtils.getField(Foo.class, "VALUE")));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(FieldUtils.getField(PublicChild.class, "VALUE")));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadNamedStaticField
    public void testReadNamedStaticField() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(Foo.class, "VALUE"));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PubliclyShadowedChild.class, "VALUE"));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PrivatelyShadowedChild.class, "VALUE"));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PublicChild.class, "VALUE"));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadNamedStaticFieldForceAccess
    public void testReadNamedStaticFieldForceAccess() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(Foo.class, "VALUE", true));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PubliclyShadowedChild.class, "VALUE", true));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PrivatelyShadowedChild.class, "VALUE", true));
        assertEquals("child", FieldUtils.readStaticField(PublicChild.class, "VALUE", true));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadDeclaredNamedStaticField
    public void testReadDeclaredNamedStaticField() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readDeclaredStaticField(Foo.class, "VALUE"));
        try {
            assertEquals("child", FieldUtils.readDeclaredStaticField(PublicChild.class, "VALUE"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(Foo.VALUE, FieldUtils.readDeclaredStaticField(PubliclyShadowedChild.class, "VALUE"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(Foo.VALUE, FieldUtils.readDeclaredStaticField(PrivatelyShadowedChild.class, "VALUE"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadDeclaredNamedStaticFieldForceAccess
    public void testReadDeclaredNamedStaticFieldForceAccess() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readDeclaredStaticField(Foo.class, "VALUE", true));
        assertEquals("child", FieldUtils.readDeclaredStaticField(PublicChild.class, "VALUE", true));
        try {
            assertEquals(Foo.VALUE, FieldUtils.readDeclaredStaticField(PubliclyShadowedChild.class, "VALUE", true));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(Foo.VALUE, FieldUtils.readDeclaredStaticField(PrivatelyShadowedChild.class, "VALUE", true));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadField
    public void testReadField() throws Exception {
        Field parentS = FieldUtils.getDeclaredField(parentClass, "s");
        assertEquals("s", FieldUtils.readField(parentS, publicChild));
        assertEquals("s", FieldUtils.readField(parentS, publiclyShadowedChild));
        assertEquals("s", FieldUtils.readField(parentS, privatelyShadowedChild));
        Field parentB = FieldUtils.getDeclaredField(parentClass, "b", true);
        assertEquals(Boolean.FALSE, FieldUtils.readField(parentB, publicChild));
        assertEquals(Boolean.FALSE, FieldUtils.readField(parentB, publiclyShadowedChild));
        assertEquals(Boolean.FALSE, FieldUtils.readField(parentB, privatelyShadowedChild));
        Field parentI = FieldUtils.getDeclaredField(parentClass, "i", true);
        assertEquals(I0, FieldUtils.readField(parentI, publicChild));
        assertEquals(I0, FieldUtils.readField(parentI, publiclyShadowedChild));
        assertEquals(I0, FieldUtils.readField(parentI, privatelyShadowedChild));
        Field parentD = FieldUtils.getDeclaredField(parentClass, "d", true);
        assertEquals(D0, FieldUtils.readField(parentD, publicChild));
        assertEquals(D0, FieldUtils.readField(parentD, publiclyShadowedChild));
        assertEquals(D0, FieldUtils.readField(parentD, privatelyShadowedChild));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadFieldForceAccess
    public void testReadFieldForceAccess() throws Exception {
        Field parentS = FieldUtils.getDeclaredField(parentClass, "s");
        parentS.setAccessible(false);
        assertEquals("s", FieldUtils.readField(parentS, publicChild, true));
        assertEquals("s", FieldUtils.readField(parentS, publiclyShadowedChild, true));
        assertEquals("s", FieldUtils.readField(parentS, privatelyShadowedChild, true));
        Field parentB = FieldUtils.getDeclaredField(parentClass, "b", true);
        parentB.setAccessible(false);
        assertEquals(Boolean.FALSE, FieldUtils.readField(parentB, publicChild, true));
        assertEquals(Boolean.FALSE, FieldUtils.readField(parentB, publiclyShadowedChild, true));
        assertEquals(Boolean.FALSE, FieldUtils.readField(parentB, privatelyShadowedChild, true));
        Field parentI = FieldUtils.getDeclaredField(parentClass, "i", true);
        parentI.setAccessible(false);
        assertEquals(I0, FieldUtils.readField(parentI, publicChild, true));
        assertEquals(I0, FieldUtils.readField(parentI, publiclyShadowedChild, true));
        assertEquals(I0, FieldUtils.readField(parentI, privatelyShadowedChild, true));
        Field parentD = FieldUtils.getDeclaredField(parentClass, "d", true);
        parentD.setAccessible(false);
        assertEquals(D0, FieldUtils.readField(parentD, publicChild, true));
        assertEquals(D0, FieldUtils.readField(parentD, publiclyShadowedChild, true));
        assertEquals(D0, FieldUtils.readField(parentD, privatelyShadowedChild, true));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadNamedField
    public void testReadNamedField() throws Exception {
        assertEquals("s", FieldUtils.readField(publicChild, "s"));
        assertEquals("ss", FieldUtils.readField(publiclyShadowedChild, "s"));
        assertEquals("s", FieldUtils.readField(privatelyShadowedChild, "s"));
        try {
            assertEquals(Boolean.FALSE, FieldUtils.readField(publicChild, "b"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(Boolean.TRUE, FieldUtils.readField(publiclyShadowedChild, "b"));
        try {
            assertEquals(Boolean.FALSE, FieldUtils.readField(privatelyShadowedChild, "b"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(I0, FieldUtils.readField(publicChild, "i"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(I1, FieldUtils.readField(publiclyShadowedChild, "i"));
        try {
            assertEquals(I0, FieldUtils.readField(privatelyShadowedChild, "i"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(D0, FieldUtils.readField(publicChild, "d"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(D1, FieldUtils.readField(publiclyShadowedChild, "d"));
        try {
            assertEquals(D0, FieldUtils.readField(privatelyShadowedChild, "d"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadNamedFieldForceAccess
    public void testReadNamedFieldForceAccess() throws Exception {
        assertEquals("s", FieldUtils.readField(publicChild, "s", true));
        assertEquals("ss", FieldUtils.readField(publiclyShadowedChild, "s", true));
        assertEquals("ss", FieldUtils.readField(privatelyShadowedChild, "s", true));
        assertEquals(Boolean.FALSE, FieldUtils.readField(publicChild, "b", true));
        assertEquals(Boolean.TRUE, FieldUtils.readField(publiclyShadowedChild, "b", true));
        assertEquals(Boolean.TRUE, FieldUtils.readField(privatelyShadowedChild, "b", true));
        assertEquals(I0, FieldUtils.readField(publicChild, "i", true));
        assertEquals(I1, FieldUtils.readField(publiclyShadowedChild, "i", true));
        assertEquals(I1, FieldUtils.readField(privatelyShadowedChild, "i", true));
        assertEquals(D0, FieldUtils.readField(publicChild, "d", true));
        assertEquals(D1, FieldUtils.readField(publiclyShadowedChild, "d", true));
        assertEquals(D1, FieldUtils.readField(privatelyShadowedChild, "d", true));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadDeclaredNamedField
    public void testReadDeclaredNamedField() throws Exception {
        try {
            assertEquals("s", FieldUtils.readDeclaredField(publicChild, "s"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals("ss", FieldUtils.readDeclaredField(publiclyShadowedChild, "s"));
        try {
            assertEquals("s", FieldUtils.readDeclaredField(privatelyShadowedChild, "s"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(Boolean.FALSE, FieldUtils.readDeclaredField(publicChild, "b"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(Boolean.TRUE, FieldUtils.readDeclaredField(publiclyShadowedChild, "b"));
        try {
            assertEquals(Boolean.FALSE, FieldUtils.readDeclaredField(privatelyShadowedChild, "b"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(I0, FieldUtils.readDeclaredField(publicChild, "i"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(I1, FieldUtils.readDeclaredField(publiclyShadowedChild, "i"));
        try {
            assertEquals(I0, FieldUtils.readDeclaredField(privatelyShadowedChild, "i"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            assertEquals(D0, FieldUtils.readDeclaredField(publicChild, "d"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(D1, FieldUtils.readDeclaredField(publiclyShadowedChild, "d"));
        try {
            assertEquals(D0, FieldUtils.readDeclaredField(privatelyShadowedChild, "d"));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testReadDeclaredNamedFieldForceAccess
    public void testReadDeclaredNamedFieldForceAccess() throws Exception {
        try {
            assertEquals("s", FieldUtils.readDeclaredField(publicChild, "s", true));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals("ss", FieldUtils.readDeclaredField(publiclyShadowedChild, "s", true));
        assertEquals("ss", FieldUtils.readDeclaredField(privatelyShadowedChild, "s", true));
        try {
            assertEquals(Boolean.FALSE, FieldUtils.readDeclaredField(publicChild, "b", true));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(Boolean.TRUE, FieldUtils.readDeclaredField(publiclyShadowedChild, "b", true));
        assertEquals(Boolean.TRUE, FieldUtils.readDeclaredField(privatelyShadowedChild, "b", true));
        try {
            assertEquals(I0, FieldUtils.readDeclaredField(publicChild, "i", true));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(I1, FieldUtils.readDeclaredField(publiclyShadowedChild, "i", true));
        assertEquals(I1, FieldUtils.readDeclaredField(privatelyShadowedChild, "i", true));
        try {
            assertEquals(D0, FieldUtils.readDeclaredField(publicChild, "d", true));
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        assertEquals(D1, FieldUtils.readDeclaredField(publiclyShadowedChild, "d", true));
        assertEquals(D1, FieldUtils.readDeclaredField(privatelyShadowedChild, "d", true));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteStaticField
    public void testWriteStaticField() throws Exception {
        Field field = StaticContainer.class.getDeclaredField("mutablePublic");
        FieldUtils.writeStaticField(field, "new");
        assertEquals("new", StaticContainer.mutablePublic);
        field = StaticContainer.class.getDeclaredField("mutableProtected");
        try {
            FieldUtils.writeStaticField(field, "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("mutablePackage");
        try {
            FieldUtils.writeStaticField(field, "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("mutablePrivate");
        try {
            FieldUtils.writeStaticField(field, "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PUBLIC");
        try {
            FieldUtils.writeStaticField(field, "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PROTECTED");
        try {
            FieldUtils.writeStaticField(field, "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PACKAGE");
        try {
            FieldUtils.writeStaticField(field, "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PRIVATE");
        try {
            FieldUtils.writeStaticField(field, "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteStaticFieldForceAccess
    public void testWriteStaticFieldForceAccess() throws Exception {
        Field field = StaticContainer.class.getDeclaredField("mutablePublic");
        FieldUtils.writeStaticField(field, "new", true);
        assertEquals("new", StaticContainer.mutablePublic);
        field = StaticContainer.class.getDeclaredField("mutableProtected");
        FieldUtils.writeStaticField(field, "new", true);
        assertEquals("new", StaticContainer.getMutableProtected());
        field = StaticContainer.class.getDeclaredField("mutablePackage");
        FieldUtils.writeStaticField(field, "new", true);
        assertEquals("new", StaticContainer.getMutablePackage());
        field = StaticContainer.class.getDeclaredField("mutablePrivate");
        FieldUtils.writeStaticField(field, "new", true);
        assertEquals("new", StaticContainer.getMutablePrivate());
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PUBLIC");
        try {
            FieldUtils.writeStaticField(field, "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PROTECTED");
        try {
            FieldUtils.writeStaticField(field, "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PACKAGE");
        try {
            FieldUtils.writeStaticField(field, "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = StaticContainer.class.getDeclaredField("IMMUTABLE_PRIVATE");
        try {
            FieldUtils.writeStaticField(field, "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteNamedStaticField
    public void testWriteNamedStaticField() throws Exception {
        FieldUtils.writeStaticField(StaticContainerChild.class, "mutablePublic", "new");
        assertEquals("new", StaticContainer.mutablePublic);
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "mutableProtected", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "mutablePackage", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "mutablePrivate", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PUBLIC", "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PROTECTED", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PACKAGE", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PRIVATE", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteNamedStaticFieldForceAccess
    public void testWriteNamedStaticFieldForceAccess() throws Exception {
        FieldUtils.writeStaticField(StaticContainerChild.class, "mutablePublic", "new", true);
        assertEquals("new", StaticContainer.mutablePublic);
        FieldUtils.writeStaticField(StaticContainerChild.class, "mutableProtected", "new", true);
        assertEquals("new", StaticContainer.getMutableProtected());
        FieldUtils.writeStaticField(StaticContainerChild.class, "mutablePackage", "new", true);
        assertEquals("new", StaticContainer.getMutablePackage());
        FieldUtils.writeStaticField(StaticContainerChild.class, "mutablePrivate", "new", true);
        assertEquals("new", StaticContainer.getMutablePrivate());
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PUBLIC", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PROTECTED", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PACKAGE", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeStaticField(StaticContainerChild.class, "IMMUTABLE_PRIVATE", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteDeclaredNamedStaticField
    public void testWriteDeclaredNamedStaticField() throws Exception {
        FieldUtils.writeStaticField(StaticContainer.class, "mutablePublic", "new");
        assertEquals("new", StaticContainer.mutablePublic);
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "mutableProtected", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "mutablePackage", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "mutablePrivate", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PUBLIC", "new");
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PROTECTED", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PACKAGE", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PRIVATE", "new");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteDeclaredNamedStaticFieldForceAccess
    public void testWriteDeclaredNamedStaticFieldForceAccess() throws Exception {
        FieldUtils.writeDeclaredStaticField(StaticContainer.class, "mutablePublic", "new", true);
        assertEquals("new", StaticContainer.mutablePublic);
        FieldUtils.writeDeclaredStaticField(StaticContainer.class, "mutableProtected", "new", true);
        assertEquals("new", StaticContainer.getMutableProtected());
        FieldUtils.writeDeclaredStaticField(StaticContainer.class, "mutablePackage", "new", true);
        assertEquals("new", StaticContainer.getMutablePackage());
        FieldUtils.writeDeclaredStaticField(StaticContainer.class, "mutablePrivate", "new", true);
        assertEquals("new", StaticContainer.getMutablePrivate());
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PUBLIC", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PROTECTED", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PACKAGE", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        try {
            FieldUtils.writeDeclaredStaticField(StaticContainer.class, "IMMUTABLE_PRIVATE", "new", true);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteField
    public void testWriteField() throws Exception {
        Field field = parentClass.getDeclaredField("s");
        FieldUtils.writeField(field, publicChild, "S");
        assertEquals("S", field.get(publicChild));
        field = parentClass.getDeclaredField("b");
        try {
            FieldUtils.writeField(field, publicChild, Boolean.TRUE);
            fail("Expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            
        }
        field = parentClass.getDeclaredField("i");
        try {
            FieldUtils.writeField(field, publicChild, new Integer(Integer.MAX_VALUE));
        } catch (IllegalAccessException e) {
            
        }
        field = parentClass.getDeclaredField("d");
        try {
            FieldUtils.writeField(field, publicChild, new Double(Double.MAX_VALUE));
        } catch (IllegalAccessException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteFieldForceAccess
    public void testWriteFieldForceAccess() throws Exception {
        Field field = parentClass.getDeclaredField("s");
        FieldUtils.writeField(field, publicChild, "S", true);
        assertEquals("S", field.get(publicChild));
        field = parentClass.getDeclaredField("b");
        FieldUtils.writeField(field, publicChild, Boolean.TRUE, true);
        assertEquals(Boolean.TRUE, field.get(publicChild));
        field = parentClass.getDeclaredField("i");
        FieldUtils.writeField(field, publicChild, new Integer(Integer.MAX_VALUE), true);
        assertEquals(new Integer(Integer.MAX_VALUE), field.get(publicChild));
        field = parentClass.getDeclaredField("d");
        FieldUtils.writeField(field, publicChild, new Double(Double.MAX_VALUE), true);
        assertEquals(new Double(Double.MAX_VALUE), field.get(publicChild));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteNamedField
    public void testWriteNamedField() throws Exception {
        FieldUtils.writeField(publicChild, "s", "S");
        assertEquals("S", FieldUtils.readField(publicChild, "s"));
        try {
            FieldUtils.writeField(publicChild, "b", Boolean.TRUE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeField(publicChild, "i", new Integer(1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeField(publicChild, "d", new Double(1.0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        FieldUtils.writeField(publiclyShadowedChild, "s", "S");
        assertEquals("S", FieldUtils.readField(publiclyShadowedChild, "s"));
        FieldUtils.writeField(publiclyShadowedChild, "b", Boolean.FALSE);
        assertEquals(Boolean.FALSE, FieldUtils.readField(publiclyShadowedChild, "b"));
        FieldUtils.writeField(publiclyShadowedChild, "i", new Integer(0));
        assertEquals(new Integer(0), FieldUtils.readField(publiclyShadowedChild, "i"));
        FieldUtils.writeField(publiclyShadowedChild, "d", new Double(0.0));
        assertEquals(new Double(0.0), FieldUtils.readField(publiclyShadowedChild, "d"));

        FieldUtils.writeField(privatelyShadowedChild, "s", "S");
        assertEquals("S", FieldUtils.readField(privatelyShadowedChild, "s"));
        try {
            FieldUtils.writeField(privatelyShadowedChild, "b", Boolean.TRUE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeField(privatelyShadowedChild, "i", new Integer(1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeField(privatelyShadowedChild, "d", new Double(1.0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteNamedFieldForceAccess
    public void testWriteNamedFieldForceAccess() throws Exception {
        FieldUtils.writeField(publicChild, "s", "S", true);
        assertEquals("S", FieldUtils.readField(publicChild, "s", true));
        FieldUtils.writeField(publicChild, "b", Boolean.TRUE, true);
        assertEquals(Boolean.TRUE, FieldUtils.readField(publicChild, "b", true));
        FieldUtils.writeField(publicChild, "i", new Integer(1), true);
        assertEquals(new Integer(1), FieldUtils.readField(publicChild, "i", true));
        FieldUtils.writeField(publicChild, "d", new Double(1.0), true);
        assertEquals(new Double(1.0), FieldUtils.readField(publicChild, "d", true));

        FieldUtils.writeField(publiclyShadowedChild, "s", "S", true);
        assertEquals("S", FieldUtils.readField(publiclyShadowedChild, "s", true));
        FieldUtils.writeField(publiclyShadowedChild, "b", Boolean.FALSE, true);
        assertEquals(Boolean.FALSE, FieldUtils.readField(publiclyShadowedChild, "b", true));
        FieldUtils.writeField(publiclyShadowedChild, "i", new Integer(0), true);
        assertEquals(new Integer(0), FieldUtils.readField(publiclyShadowedChild, "i", true));
        FieldUtils.writeField(publiclyShadowedChild, "d", new Double(0.0), true);
        assertEquals(new Double(0.0), FieldUtils.readField(publiclyShadowedChild, "d", true));

        FieldUtils.writeField(privatelyShadowedChild, "s", "S", true);
        assertEquals("S", FieldUtils.readField(privatelyShadowedChild, "s", true));
        FieldUtils.writeField(privatelyShadowedChild, "b", Boolean.FALSE, true);
        assertEquals(Boolean.FALSE, FieldUtils.readField(privatelyShadowedChild, "b", true));
        FieldUtils.writeField(privatelyShadowedChild, "i", new Integer(0), true);
        assertEquals(new Integer(0), FieldUtils.readField(privatelyShadowedChild, "i", true));
        FieldUtils.writeField(privatelyShadowedChild, "d", new Double(0.0), true);
        assertEquals(new Double(0.0), FieldUtils.readField(privatelyShadowedChild, "d", true));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteDeclaredNamedField
    public void testWriteDeclaredNamedField() throws Exception {
        try {
            FieldUtils.writeDeclaredField(publicChild, "s", "S");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(publicChild, "b", Boolean.TRUE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(publicChild, "i", new Integer(1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(publicChild, "d", new Double(1.0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        FieldUtils.writeDeclaredField(publiclyShadowedChild, "s", "S");
        assertEquals("S", FieldUtils.readDeclaredField(publiclyShadowedChild, "s"));
        FieldUtils.writeDeclaredField(publiclyShadowedChild, "b", Boolean.FALSE);
        assertEquals(Boolean.FALSE, FieldUtils.readDeclaredField(publiclyShadowedChild, "b"));
        FieldUtils.writeDeclaredField(publiclyShadowedChild, "i", new Integer(0));
        assertEquals(new Integer(0), FieldUtils.readDeclaredField(publiclyShadowedChild, "i"));
        FieldUtils.writeDeclaredField(publiclyShadowedChild, "d", new Double(0.0));
        assertEquals(new Double(0.0), FieldUtils.readDeclaredField(publiclyShadowedChild, "d"));

        try {
            FieldUtils.writeDeclaredField(privatelyShadowedChild, "s", "S");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(privatelyShadowedChild, "b", Boolean.TRUE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(privatelyShadowedChild, "i", new Integer(1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(privatelyShadowedChild, "d", new Double(1.0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testWriteDeclaredNamedFieldForceAccess
    public void testWriteDeclaredNamedFieldForceAccess() throws Exception {
        try {
            FieldUtils.writeDeclaredField(publicChild, "s", "S", true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(publicChild, "b", Boolean.TRUE, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(publicChild, "i", new Integer(1), true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }
        try {
            FieldUtils.writeDeclaredField(publicChild, "d", new Double(1.0), true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            
        }

        FieldUtils.writeDeclaredField(publiclyShadowedChild, "s", "S", true);
        assertEquals("S", FieldUtils.readDeclaredField(publiclyShadowedChild, "s", true));
        FieldUtils.writeDeclaredField(publiclyShadowedChild, "b", Boolean.FALSE, true);
        assertEquals(Boolean.FALSE, FieldUtils.readDeclaredField(publiclyShadowedChild, "b", true));
        FieldUtils.writeDeclaredField(publiclyShadowedChild, "i", new Integer(0), true);
        assertEquals(new Integer(0), FieldUtils.readDeclaredField(publiclyShadowedChild, "i", true));
        FieldUtils.writeDeclaredField(publiclyShadowedChild, "d", new Double(0.0), true);
        assertEquals(new Double(0.0), FieldUtils.readDeclaredField(publiclyShadowedChild, "d", true));

        FieldUtils.writeDeclaredField(privatelyShadowedChild, "s", "S", true);
        assertEquals("S", FieldUtils.readDeclaredField(privatelyShadowedChild, "s", true));
        FieldUtils.writeDeclaredField(privatelyShadowedChild, "b", Boolean.FALSE, true);
        assertEquals(Boolean.FALSE, FieldUtils.readDeclaredField(privatelyShadowedChild, "b", true));
        FieldUtils.writeDeclaredField(privatelyShadowedChild, "i", new Integer(0), true);
        assertEquals(new Integer(0), FieldUtils.readDeclaredField(privatelyShadowedChild, "i", true));
        FieldUtils.writeDeclaredField(privatelyShadowedChild, "d", new Double(0.0), true);
        assertEquals(new Double(0.0), FieldUtils.readDeclaredField(privatelyShadowedChild, "d", true));
    }

// org.apache.commons.lang.reflect.FieldUtilsTest::testAmbig
    public void testAmbig() {
        try {
            FieldUtils.getField(Ambig.class, "VALUE");
            fail("should have failed on interface field ambiguity");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testConstructor
    public void testConstructor() throws Exception {
        assertNotNull(MethodUtils.class.newInstance());
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testInvokeMethod
    public void testInvokeMethod() throws Exception {
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                (Class[]) null));
        assertEquals("foo(String)", MethodUtils.invokeMethod(testBean, "foo",
                ""));
        assertEquals("foo(Object)", MethodUtils.invokeMethod(testBean, "foo",
                new Object()));
        assertEquals("foo(Object)", MethodUtils.invokeMethod(testBean, "foo",
                Boolean.TRUE));
        assertEquals("foo(Integer)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.INTEGER_ONE));
        assertEquals("foo(int)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.BYTE_ONE));
        assertEquals("foo(double)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.LONG_ONE));
        assertEquals("foo(double)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.DOUBLE_ONE));
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testInvokeExactMethod
    public void testInvokeExactMethod() throws Exception {
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                (Class[]) null));
        assertEquals("foo(String)", MethodUtils.invokeExactMethod(testBean,
                "foo", ""));
        assertEquals("foo(Object)", MethodUtils.invokeExactMethod(testBean,
                "foo", new Object()));
        assertEquals("foo(Integer)", MethodUtils.invokeExactMethod(testBean,
                "foo", NumberUtils.INTEGER_ONE));
        assertEquals("foo(double)", MethodUtils.invokeExactMethod(testBean,
                "foo", new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }));

        try {
            MethodUtils
                    .invokeExactMethod(testBean, "foo", NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils
                    .invokeExactMethod(testBean, "foo", NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactMethod(testBean, "foo", Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testInvokeStaticMethod
    public void testInvokeStaticMethod() throws Exception {
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", (Class[]) null));
        assertEquals("bar(String)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", ""));
        assertEquals("bar(Object)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", new Object()));
        assertEquals("bar(Object)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", Boolean.TRUE));
        assertEquals("bar(Integer)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.INTEGER_ONE));
        assertEquals("bar(int)", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", NumberUtils.BYTE_ONE));
        assertEquals("bar(double)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.LONG_ONE));
        assertEquals("bar(double)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.DOUBLE_ONE));
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testInvokeExactStaticMethod
    public void testInvokeExactStaticMethod() throws Exception {
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", (Class[]) null));
        assertEquals("bar(String)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", ""));
        assertEquals("bar(Object)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", new Object()));
        assertEquals("bar(Integer)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", NumberUtils.INTEGER_ONE));
        assertEquals("bar(double)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }));

        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (NoSuchMethodException e) {
        }
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testGetAccessibleInterfaceMethod
    public void testGetAccessibleInterfaceMethod() throws Exception {

        Class[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (int i = 0; i < p.length; i++) {
            Method method = TestMutable.class.getMethod("getValue", p[i]);
            Method accessibleMethod = MethodUtils.getAccessibleMethod(method);
            assertNotSame(accessibleMethod, method);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testGetAccessibleInterfaceMethodFromDescription
    public void testGetAccessibleInterfaceMethodFromDescription()
            throws Exception {
        Class[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (int i = 0; i < p.length; i++) {
            Method accessibleMethod = MethodUtils.getAccessibleMethod(
                    TestMutable.class, "getValue", p[i]);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testGetAccessiblePublicMethod
    public void testGetAccessiblePublicMethod() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class.getMethod("getValue",
                        ArrayUtils.EMPTY_CLASS_ARRAY)).getDeclaringClass());
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testGetAccessiblePublicMethodFromDescription
    public void testGetAccessiblePublicMethodFromDescription() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class, "getValue", ArrayUtils.EMPTY_CLASS_ARRAY)
                .getDeclaringClass());
    }

// org.apache.commons.lang.reflect.MethodUtilsTest::testGetMatchingAccessibleMethod
    public void testGetMatchingAccessibleMethod() throws Exception {
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                null, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(String.class), singletonArray(String.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Object.class), singletonArray(Object.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Boolean.class), singletonArray(Object.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Byte.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Byte.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Short.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Short.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Character.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Character.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Integer.class), singletonArray(Integer.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Integer.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Long.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Long.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Float.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Float.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Double.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Double.TYPE), singletonArray(Double.TYPE));
    }
