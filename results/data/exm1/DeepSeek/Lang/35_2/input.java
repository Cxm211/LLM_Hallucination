// buggy code
    public static <T> T[] add(T[] array, T element) {
        Class<?> type;
        if (array != null){
            type = array.getClass();
        } else if (element != null) {
            type = element.getClass();
        } else {
            type = Object.class;
        }
        @SuppressWarnings("unchecked") // type must be T
        T[] newArray = (T[]) copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    public static <T> T[] add(T[] array, int index, T element) {
        Class<?> clss = null;
        if (array != null) {
            clss = array.getClass().getComponentType();
        } else if (element != null) {
            clss = element.getClass();
        } else {
            return (T[]) new Object[] { null };
        }
        @SuppressWarnings("unchecked") // the add method creates an array of type clss, which is type T
        final T[] newArray = (T[]) add(array, index, element, clss);
        return newArray;
    }

// relevant test
// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_null
    public void testGetThrowables_Throwable_null() {
        assertEquals(0, ExceptionUtils.getThrowables(null).length);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_withoutCause
    public void testGetThrowables_Throwable_withoutCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(withoutCause);
        assertEquals(1, throwables.length);
        assertSame(withoutCause, throwables[0]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_nested
    public void testGetThrowables_Throwable_nested() {
        Throwable[] throwables = ExceptionUtils.getThrowables(nested);
        assertEquals(2, throwables.length);
        assertSame(nested, throwables[0]);
        assertSame(withoutCause, throwables[1]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_withCause
    public void testGetThrowables_Throwable_withCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(withCause);
        assertEquals(3, throwables.length);
        assertSame(withCause, throwables[0]);
        assertSame(nested, throwables[1]);
        assertSame(withoutCause, throwables[2]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_jdkNoCause
    public void testGetThrowables_Throwable_jdkNoCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(jdkNoCause);
        assertEquals(1, throwables.length);
        assertSame(jdkNoCause, throwables[0]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowables_Throwable_recursiveCause
    public void testGetThrowables_Throwable_recursiveCause() {
        Throwable[] throwables = ExceptionUtils.getThrowables(cyclicCause);
        assertEquals(3, throwables.length);
        assertSame(cyclicCause, throwables[0]);
        assertSame(cyclicCause.getCause(), throwables[1]);
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), throwables[2]);
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_null
    public void testGetThrowableList_Throwable_null() {
        List<?> throwables = ExceptionUtils.getThrowableList(null);
        assertEquals(0, throwables.size());
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_withoutCause
    public void testGetThrowableList_Throwable_withoutCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(withoutCause);
        assertEquals(1, throwables.size());
        assertSame(withoutCause, throwables.get(0));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_nested
    public void testGetThrowableList_Throwable_nested() {
        List<?> throwables = ExceptionUtils.getThrowableList(nested);
        assertEquals(2, throwables.size());
        assertSame(nested, throwables.get(0));
        assertSame(withoutCause, throwables.get(1));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_withCause
    public void testGetThrowableList_Throwable_withCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(withCause);
        assertEquals(3, throwables.size());
        assertSame(withCause, throwables.get(0));
        assertSame(nested, throwables.get(1));
        assertSame(withoutCause, throwables.get(2));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_jdkNoCause
    public void testGetThrowableList_Throwable_jdkNoCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(jdkNoCause);
        assertEquals(1, throwables.size());
        assertSame(jdkNoCause, throwables.get(0));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetThrowableList_Throwable_recursiveCause
    public void testGetThrowableList_Throwable_recursiveCause() {
        List<?> throwables = ExceptionUtils.getThrowableList(cyclicCause);
        assertEquals(3, throwables.size());
        assertSame(cyclicCause, throwables.get(0));
        assertSame(cyclicCause.getCause(), throwables.get(1));
        assertSame(((ExceptionWithCause) cyclicCause.getCause()).getCause(), throwables.get(2));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOf_ThrowableClass
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

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOf_ThrowableClassInt
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

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOfType_ThrowableClass
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

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testIndexOfType_ThrowableClassInt
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

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_Throwable
    public void testPrintRootCauseStackTrace_Throwable() throws Exception {
        ExceptionUtils.printRootCauseStackTrace(null);
        
        
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_ThrowableStream
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

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testPrintRootCauseStackTrace_ThrowableWriter
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

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testGetRootCauseStackTrace_Throwable
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

// org.apache.commons.lang3.exception.ExceptionUtilsTest::testRemoveCommonFrames_ListList
    public void testRemoveCommonFrames_ListList() throws Exception {
        try {
            ExceptionUtils.removeCommonFrames(null, null);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::test_getMessage_Throwable
    public void test_getMessage_Throwable() {
        Throwable th = null;
        assertEquals("", ExceptionUtils.getMessage(th));
        
        th = new IllegalArgumentException("Base");
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getMessage(th));
        
        th = new ExceptionWithCause("Wrapper", th);
        assertEquals("ExceptionUtilsTest.ExceptionWithCause: Wrapper", ExceptionUtils.getMessage(th));
    }

// org.apache.commons.lang3.exception.ExceptionUtilsTest::test_getRootCauseMessage_Throwable
    public void test_getRootCauseMessage_Throwable() {
        Throwable th = null;
        assertEquals("", ExceptionUtils.getRootCauseMessage(th));
        
        th = new IllegalArgumentException("Base");
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getRootCauseMessage(th));
        
        th = new ExceptionWithCause("Wrapper", th);
        assertEquals("IllegalArgumentException: Base", ExceptionUtils.getRootCauseMessage(th));
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testConstructor
    public void testConstructor() throws Exception {
        assertNotNull(MethodUtils.class.newInstance());
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testInvokeConstructor
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

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testInvokeExactConstructor
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

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testGetAccessibleConstructor
    public void testGetAccessibleConstructor() throws Exception {
        assertNotNull(ConstructorUtils.getAccessibleConstructor(Object.class
                .getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY)));
        assertNull(ConstructorUtils.getAccessibleConstructor(PrivateClass.class
                .getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY)));
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testGetAccessibleConstructorFromDescription
    public void testGetAccessibleConstructorFromDescription() throws Exception {
        assertNotNull(ConstructorUtils.getAccessibleConstructor(Object.class,
                ArrayUtils.EMPTY_CLASS_ARRAY));
        assertNull(ConstructorUtils.getAccessibleConstructor(
                PrivateClass.class, ArrayUtils.EMPTY_CLASS_ARRAY));
    }

// org.apache.commons.lang3.reflect.ConstructorUtilsTest::testGetMatchingAccessibleMethod
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testGetField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testGetFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testGetDeclaredField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testGetDeclaredFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadStaticField
    public void testReadStaticField() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(FieldUtils.getField(Foo.class, "VALUE")));
    }

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadStaticFieldForceAccess
    public void testReadStaticFieldForceAccess() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(FieldUtils.getField(Foo.class, "VALUE")));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(FieldUtils.getField(PublicChild.class, "VALUE")));
    }

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadNamedStaticField
    public void testReadNamedStaticField() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(Foo.class, "VALUE"));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PubliclyShadowedChild.class, "VALUE"));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PrivatelyShadowedChild.class, "VALUE"));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PublicChild.class, "VALUE"));
    }

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadNamedStaticFieldForceAccess
    public void testReadNamedStaticFieldForceAccess() throws Exception {
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(Foo.class, "VALUE", true));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PubliclyShadowedChild.class, "VALUE", true));
        assertEquals(Foo.VALUE, FieldUtils.readStaticField(PrivatelyShadowedChild.class, "VALUE", true));
        assertEquals("child", FieldUtils.readStaticField(PublicChild.class, "VALUE", true));
    }

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadDeclaredNamedStaticField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadDeclaredNamedStaticFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadNamedField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadNamedFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadDeclaredNamedField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testReadDeclaredNamedFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteStaticField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteStaticFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteNamedStaticField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteNamedStaticFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteDeclaredNamedStaticField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteDeclaredNamedStaticFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteNamedField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteNamedFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteDeclaredNamedField
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testWriteDeclaredNamedFieldForceAccess
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

// org.apache.commons.lang3.reflect.FieldUtilsTest::testAmbig
    public void testAmbig() {
        try {
            FieldUtils.getField(Ambig.class, "VALUE");
            fail("should have failed on interface field ambiguity");
        } catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testConstructor
    public void testConstructor() throws Exception {
        assertNotNull(MethodUtils.class.newInstance());
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeMethod
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

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeExactMethod
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

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeStaticMethod
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

// org.apache.commons.lang3.reflect.MethodUtilsTest::testInvokeExactStaticMethod
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

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessibleInterfaceMethod
    public void testGetAccessibleInterfaceMethod() throws Exception {

        Class<?>[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (int i = 0; i < p.length; i++) {
            Method method = TestMutable.class.getMethod("getValue", p[i]);
            Method accessibleMethod = MethodUtils.getAccessibleMethod(method);
            assertNotSame(accessibleMethod, method);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessibleInterfaceMethodFromDescription
    public void testGetAccessibleInterfaceMethodFromDescription()
            throws Exception {
        Class<?>[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (int i = 0; i < p.length; i++) {
            Method accessibleMethod = MethodUtils.getAccessibleMethod(
                    TestMutable.class, "getValue", p[i]);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessiblePublicMethod
    public void testGetAccessiblePublicMethod() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class.getMethod("getValue",
                        ArrayUtils.EMPTY_CLASS_ARRAY)).getDeclaringClass());
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetAccessiblePublicMethodFromDescription
    public void testGetAccessiblePublicMethodFromDescription() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class, "getValue", ArrayUtils.EMPTY_CLASS_ARRAY)
                .getDeclaringClass());
    }

// org.apache.commons.lang3.reflect.MethodUtilsTest::testGetMatchingAccessibleMethod
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

// org.apache.commons.lang3.text.StrBuilderTest::testConstructors
    public void testConstructors() {
        StrBuilder sb0 = new StrBuilder();
        assertEquals(32, sb0.capacity());
        assertEquals(0, sb0.length());
        assertEquals(0, sb0.size());

        StrBuilder sb1 = new StrBuilder(32);
        assertEquals(32, sb1.capacity());
        assertEquals(0, sb1.length());
        assertEquals(0, sb1.size());

        StrBuilder sb2 = new StrBuilder(0);
        assertEquals(32, sb2.capacity());
        assertEquals(0, sb2.length());
        assertEquals(0, sb2.size());

        StrBuilder sb3 = new StrBuilder(-1);
        assertEquals(32, sb3.capacity());
        assertEquals(0, sb3.length());
        assertEquals(0, sb3.size());

        StrBuilder sb4 = new StrBuilder(1);
        assertEquals(1, sb4.capacity());
        assertEquals(0, sb4.length());
        assertEquals(0, sb4.size());

        StrBuilder sb5 = new StrBuilder((String) null);
        assertEquals(32, sb5.capacity());
        assertEquals(0, sb5.length());
        assertEquals(0, sb5.size());

        StrBuilder sb6 = new StrBuilder("");
        assertEquals(32, sb6.capacity());
        assertEquals(0, sb6.length());
        assertEquals(0, sb6.size());

        StrBuilder sb7 = new StrBuilder("foo");
        assertEquals(35, sb7.capacity());
        assertEquals(3, sb7.length());
        assertEquals(3, sb7.size());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testChaining
    public void testChaining() {
        StrBuilder sb = new StrBuilder();
        assertSame(sb, sb.setNewLineText(null));
        assertSame(sb, sb.setNullText(null));
        assertSame(sb, sb.setLength(1));
        assertSame(sb, sb.setCharAt(0, 'a'));
        assertSame(sb, sb.ensureCapacity(0));
        assertSame(sb, sb.minimizeCapacity());
        assertSame(sb, sb.clear());
        assertSame(sb, sb.reverse());
        assertSame(sb, sb.trim());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetSetNewLineText
    public void testGetSetNewLineText() {
        StrBuilder sb = new StrBuilder();
        assertEquals(null, sb.getNewLineText());

        sb.setNewLineText("#");
        assertEquals("#", sb.getNewLineText());

        sb.setNewLineText("");
        assertEquals("", sb.getNewLineText());

        sb.setNewLineText((String) null);
        assertEquals(null, sb.getNewLineText());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetSetNullText
    public void testGetSetNullText() {
        StrBuilder sb = new StrBuilder();
        assertEquals(null, sb.getNullText());

        sb.setNullText("null");
        assertEquals("null", sb.getNullText());

        sb.setNullText("");
        assertEquals(null, sb.getNullText());

        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());

        sb.setNullText((String) null);
        assertEquals(null, sb.getNullText());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testCapacityAndLength
    public void testCapacityAndLength() {
        StrBuilder sb = new StrBuilder();
        assertEquals(32, sb.capacity());
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.minimizeCapacity();
        assertEquals(0, sb.capacity());
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.ensureCapacity(32);
        assertTrue(sb.capacity() >= 32);
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.append("foo");
        assertTrue(sb.capacity() >= 32);
        assertEquals(3, sb.length());
        assertEquals(3, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.clear();
        assertTrue(sb.capacity() >= 32);
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());

        sb.append("123456789012345678901234567890123");
        assertTrue(sb.capacity() > 32);
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.ensureCapacity(16);
        assertTrue(sb.capacity() > 16);
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.minimizeCapacity();
        assertEquals(33, sb.capacity());
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        try {
            sb.setLength(-1);
            fail("setLength(-1) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }

        sb.setLength(33);
        assertEquals(33, sb.capacity());
        assertEquals(33, sb.length());
        assertEquals(33, sb.size());
        assertTrue(sb.isEmpty() == false);

        sb.setLength(16);
        assertTrue(sb.capacity() >= 16);
        assertEquals(16, sb.length());
        assertEquals(16, sb.size());
        assertEquals("1234567890123456", sb.toString());
        assertTrue(sb.isEmpty() == false);

        sb.setLength(32);
        assertTrue(sb.capacity() >= 32);
        assertEquals(32, sb.length());
        assertEquals(32, sb.size());
        assertEquals("1234567890123456\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0", sb.toString());
        assertTrue(sb.isEmpty() == false);

        sb.setLength(0);
        assertTrue(sb.capacity() >= 32);
        assertEquals(0, sb.length());
        assertEquals(0, sb.size());
        assertTrue(sb.isEmpty());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLength
    public void testLength() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
        
        sb.append("Hello");
        assertEquals(5, sb.length());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSetLength
    public void testSetLength() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello");
        sb.setLength(2);  
        assertEquals("He", sb.toString());
        sb.setLength(2);  
        assertEquals("He", sb.toString());
        sb.setLength(3);  
        assertEquals("He\0", sb.toString());

        try {
            sb.setLength(-1);
            fail("setLength(-1) expected StringIndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testCapacity
    public void testCapacity() {
        StrBuilder sb = new StrBuilder();
        assertEquals(sb.buffer.length, sb.capacity());
        
        sb.append("HelloWorldHelloWorldHelloWorldHelloWorld");
        assertEquals(sb.buffer.length, sb.capacity());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEnsureCapacity
    public void testEnsureCapacity() {
        StrBuilder sb = new StrBuilder();
        sb.ensureCapacity(2);
        assertEquals(true, sb.capacity() >= 2);
        
        sb.ensureCapacity(-1);
        assertEquals(true, sb.capacity() >= 0);
        
        sb.append("HelloWorld");
        sb.ensureCapacity(40);
        assertEquals(true, sb.capacity() >= 40);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testMinimizeCapacity
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder();
        sb.minimizeCapacity();
        assertEquals(0, sb.capacity());
        
        sb.append("HelloWorld");
        sb.minimizeCapacity();
        assertEquals(10, sb.capacity());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSize
    public void testSize() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.size());
        
        sb.append("Hello");
        assertEquals(5, sb.size());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIsEmpty
    public void testIsEmpty() {
        StrBuilder sb = new StrBuilder();
        assertEquals(true, sb.isEmpty());
        
        sb.append("Hello");
        assertEquals(false, sb.isEmpty());
        
        sb.clear();
        assertEquals(true, sb.isEmpty());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testClear
    public void testClear() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello");
        sb.clear();
        assertEquals(0, sb.length());
        assertEquals(true, sb.buffer.length >= 5);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testCharAt
    public void testCharAt() {
        StrBuilder sb = new StrBuilder();
        try {
            sb.charAt(0);
            fail("charAt(0) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            sb.charAt(-1);
            fail("charAt(-1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        sb.append("foo");
        assertEquals('f', sb.charAt(0));
        assertEquals('o', sb.charAt(1));
        assertEquals('o', sb.charAt(2));
        try {
            sb.charAt(-1);
            fail("charAt(-1) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            sb.charAt(3);
            fail("charAt(3) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSetCharAt
    public void testSetCharAt() {
        StrBuilder sb = new StrBuilder();
        try {
            sb.setCharAt(0, 'f');
            fail("setCharAt(0,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            sb.setCharAt(-1, 'f');
            fail("setCharAt(-1,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        sb.append("foo");
        sb.setCharAt(0, 'b');
        sb.setCharAt(1, 'a');
        sb.setCharAt(2, 'r');
        try {
            sb.setCharAt(3, '!');
            fail("setCharAt(3,) expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        assertEquals("bar", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteCharAt
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(0);
        assertEquals("bc", sb.toString()); 
        
        try {
            sb.deleteCharAt(1000);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToCharArray
    public void testToCharArray() {
        StrBuilder sb = new StrBuilder();
        assertEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sb.toCharArray());

        char[] a = sb.toCharArray();
        assertNotNull("toCharArray() result is null", a);
        assertEquals("toCharArray() result is too large", 0, a.length);

        sb.append("junit");
        a = sb.toCharArray();
        assertEquals("toCharArray() result incorrect length", 5, a.length);
        assertTrue("toCharArray() result does not match", Arrays.equals("junit".toCharArray(), a));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToCharArrayIntInt
    public void testToCharArrayIntInt() {
        StrBuilder sb = new StrBuilder();
        assertEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sb.toCharArray(0, 0));

        sb.append("junit");
        char[] a = sb.toCharArray(0, 20); 
        assertEquals("toCharArray(int,int) result incorrect length", 5, a.length);
        assertTrue("toCharArray(int,int) result does not match", Arrays.equals("junit".toCharArray(), a));

        a = sb.toCharArray(0, 4);
        assertEquals("toCharArray(int,int) result incorrect length", 4, a.length);
        assertTrue("toCharArray(int,int) result does not match", Arrays.equals("juni".toCharArray(), a));

        a = sb.toCharArray(0, 4);
        assertEquals("toCharArray(int,int) result incorrect length", 4, a.length);
        assertTrue("toCharArray(int,int) result does not match", Arrays.equals("juni".toCharArray(), a));

        a = sb.toCharArray(0, 1);
        assertNotNull("toCharArray(int,int) result is null", a);

        try {
            sb.toCharArray(-1, 5);
            fail("no string index out of bound on -1");
        } catch (IndexOutOfBoundsException e) {
        }

        try {
            sb.toCharArray(6, 5);
            fail("no string index out of bound on -1");
        } catch (IndexOutOfBoundsException e) {
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetChars
    public void testGetChars ( ) {
        StrBuilder sb = new StrBuilder();
        
        char[] input = new char[10];
        char[] a = sb.getChars(input);
        assertSame (input, a);
        assertTrue(Arrays.equals(new char[10], a));
        
        sb.append("junit");
        a = sb.getChars(input);
        assertSame(input, a);
        assertTrue(Arrays.equals(new char[] {'j','u','n','i','t',0,0,0,0,0},a));
        
        a = sb.getChars(null);
        assertNotSame(input,a);
        assertEquals(5,a.length);
        assertTrue(Arrays.equals("junit".toCharArray(),a));
        
        input = new char[5];
        a = sb.getChars(input);
        assertSame(input, a);
        
        input = new char[4];
        a = sb.getChars(input);
        assertNotSame(input, a);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testGetCharsIntIntCharArrayInt
    public void testGetCharsIntIntCharArrayInt( ) {
        StrBuilder sb = new StrBuilder();
               
        sb.append("junit");
        char[] a = new char[5];
        sb.getChars(0,5,a,0);
        assertTrue(Arrays.equals(new char[] {'j','u','n','i','t'},a));
        
        a = new char[5];
        sb.getChars(0,2,a,3);
        assertTrue(Arrays.equals(new char[] {0,0,0,'j','u'},a));
        
        try {
            sb.getChars(-1,0,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            sb.getChars(0,-1,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            sb.getChars(0,20,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
        
        try {
            sb.getChars(4,2,a,0);
            fail("no exception");
        }
        catch (IndexOutOfBoundsException e) {
        }
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteIntInt
    public void testDeleteIntInt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.delete(0, 1);
        assertEquals("bc", sb.toString()); 
        sb.delete(1, 2);
        assertEquals("b", sb.toString());
        sb.delete(0, 1);
        assertEquals("", sb.toString()); 
        sb.delete(0, 1000);
        assertEquals("", sb.toString()); 
        
        try {
            sb.delete(1, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        try {
            sb.delete(-1, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        
        sb = new StrBuilder("anything");
        try {
            sb.delete(2, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteAll_char
    public void testDeleteAll_char() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.deleteAll('X');
        assertEquals("abcbccba", sb.toString());
        sb.deleteAll('a');
        assertEquals("bcbccb", sb.toString());
        sb.deleteAll('c');
        assertEquals("bbb", sb.toString());
        sb.deleteAll('b');
        assertEquals("", sb.toString());

        sb = new StrBuilder("");
        sb.deleteAll('b');
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteFirst_char
    public void testDeleteFirst_char() {
        StrBuilder sb = new StrBuilder("abcba");
        sb.deleteFirst('X');
        assertEquals("abcba", sb.toString());
        sb.deleteFirst('a');
        assertEquals("bcba", sb.toString());
        sb.deleteFirst('c');
        assertEquals("bba", sb.toString());
        sb.deleteFirst('b');
        assertEquals("ba", sb.toString());

        sb = new StrBuilder("");
        sb.deleteFirst('b');
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteAll_String
    public void testDeleteAll_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.deleteAll((String) null);
        assertEquals("abcbccba", sb.toString());
        sb.deleteAll("");
        assertEquals("abcbccba", sb.toString());
        
        sb.deleteAll("X");
        assertEquals("abcbccba", sb.toString());
        sb.deleteAll("a");
        assertEquals("bcbccb", sb.toString());
        sb.deleteAll("c");
        assertEquals("bbb", sb.toString());
        sb.deleteAll("b");
        assertEquals("", sb.toString());

        sb = new StrBuilder("abcbccba");
        sb.deleteAll("bc");
        assertEquals("acba", sb.toString());

        sb = new StrBuilder("");
        sb.deleteAll("bc");
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteFirst_String
    public void testDeleteFirst_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.deleteFirst((String) null);
        assertEquals("abcbccba", sb.toString());
        sb.deleteFirst("");
        assertEquals("abcbccba", sb.toString());

        sb.deleteFirst("X");
        assertEquals("abcbccba", sb.toString());
        sb.deleteFirst("a");
        assertEquals("bcbccba", sb.toString());
        sb.deleteFirst("c");
        assertEquals("bbccba", sb.toString());
        sb.deleteFirst("b");
        assertEquals("bccba", sb.toString());

        sb = new StrBuilder("abcbccba");
        sb.deleteFirst("bc");
        assertEquals("abccba", sb.toString());

        sb = new StrBuilder("");
        sb.deleteFirst("bc");
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteAll_StrMatcher
    public void testDeleteAll_StrMatcher() {
        StrBuilder sb = new StrBuilder("A0xA1A2yA3");
        sb.deleteAll((StrMatcher) null);
        assertEquals("A0xA1A2yA3", sb.toString());
        sb.deleteAll(A_NUMBER_MATCHER);
        assertEquals("xy", sb.toString());

        sb = new StrBuilder("Ax1");
        sb.deleteAll(A_NUMBER_MATCHER);
        assertEquals("Ax1", sb.toString());

        sb = new StrBuilder("");
        sb.deleteAll(A_NUMBER_MATCHER);
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testDeleteFirst_StrMatcher
    public void testDeleteFirst_StrMatcher() {
        StrBuilder sb = new StrBuilder("A0xA1A2yA3");
        sb.deleteFirst((StrMatcher) null);
        assertEquals("A0xA1A2yA3", sb.toString());
        sb.deleteFirst(A_NUMBER_MATCHER);
        assertEquals("xA1A2yA3", sb.toString());

        sb = new StrBuilder("Ax1");
        sb.deleteFirst(A_NUMBER_MATCHER);
        assertEquals("Ax1", sb.toString());

        sb = new StrBuilder("");
        sb.deleteFirst(A_NUMBER_MATCHER);
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_int_int_String
    public void testReplace_int_int_String() {
        StrBuilder sb = new StrBuilder("abc");
        sb.replace(0, 1, "d");
        assertEquals("dbc", sb.toString());
        sb.replace(0, 1, "aaa");
        assertEquals("aaabc", sb.toString());
        sb.replace(0, 3, "");
        assertEquals("bc", sb.toString());
        sb.replace(1, 2, (String) null);
        assertEquals("b", sb.toString());
        sb.replace(1, 1000, "text");
        assertEquals("btext", sb.toString());
        sb.replace(0, 1000, "text");
        assertEquals("text", sb.toString());
        
        sb = new StrBuilder("atext");
        sb.replace(1, 1, "ny");
        assertEquals("anytext", sb.toString());
        try {
            sb.replace(2, 1, "anything");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        
        sb = new StrBuilder();
        try {
            sb.replace(1, 2, "anything");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
        try {
            sb.replace(-1, 1, "anything");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceAll_char_char
    public void testReplaceAll_char_char() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceAll('x', 'y');
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll('a', 'd');
        assertEquals("dbcbccbd", sb.toString());
        sb.replaceAll('b', 'e');
        assertEquals("dececced", sb.toString());
        sb.replaceAll('c', 'f');
        assertEquals("defeffed", sb.toString());
        sb.replaceAll('d', 'd');
        assertEquals("defeffed", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceFirst_char_char
    public void testReplaceFirst_char_char() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceFirst('x', 'y');
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst('a', 'd');
        assertEquals("dbcbccba", sb.toString());
        sb.replaceFirst('b', 'e');
        assertEquals("decbccba", sb.toString());
        sb.replaceFirst('c', 'f');
        assertEquals("defbccba", sb.toString());
        sb.replaceFirst('d', 'd');
        assertEquals("defbccba", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceAll_String_String
    public void testReplaceAll_String_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceAll((String) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll((String) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll("", null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll("", "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceAll("x", "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll("a", "d");
        assertEquals("dbcbccbd", sb.toString());
        sb.replaceAll("d", null);
        assertEquals("bcbccb", sb.toString());
        sb.replaceAll("cb", "-");
        assertEquals("b-c-", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceAll("b", "xbx");
        assertEquals("axbxcxbxa", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceAll("b", "xbx");
        assertEquals("xbxxbx", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceFirst_String_String
    public void testReplaceFirst_String_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceFirst((String) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst((String) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst("", null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst("", "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceFirst("x", "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst("a", "d");
        assertEquals("dbcbccba", sb.toString());
        sb.replaceFirst("d", null);
        assertEquals("bcbccba", sb.toString());
        sb.replaceFirst("cb", "-");
        assertEquals("b-ccba", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceFirst("b", "xbx");
        assertEquals("axbxcba", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceFirst("b", "xbx");
        assertEquals("xbxb", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceAll_StrMatcher_String
    public void testReplaceAll_StrMatcher_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceAll((StrMatcher) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll((StrMatcher) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll(StrMatcher.noneMatcher(), null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll(StrMatcher.noneMatcher(), "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceAll(StrMatcher.charMatcher('x'), "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceAll(StrMatcher.charMatcher('a'), "d");
        assertEquals("dbcbccbd", sb.toString());
        sb.replaceAll(StrMatcher.charMatcher('d'), null);
        assertEquals("bcbccb", sb.toString());
        sb.replaceAll(StrMatcher.stringMatcher("cb"), "-");
        assertEquals("b-c-", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceAll(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("axbxcxbxa", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceAll(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("xbxxbx", sb.toString());
        
        sb = new StrBuilder("A1-A2A3-A4");
        sb.replaceAll(A_NUMBER_MATCHER, "***");
        assertEquals("***-******-***", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplaceFirst_StrMatcher_String
    public void testReplaceFirst_StrMatcher_String() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replaceFirst((StrMatcher) null, null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst((StrMatcher) null, "anything");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.noneMatcher(), null);
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.noneMatcher(), "anything");
        assertEquals("abcbccba", sb.toString());
        
        sb.replaceFirst(StrMatcher.charMatcher('x'), "y");
        assertEquals("abcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.charMatcher('a'), "d");
        assertEquals("dbcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.charMatcher('d'), null);
        assertEquals("bcbccba", sb.toString());
        sb.replaceFirst(StrMatcher.stringMatcher("cb"), "-");
        assertEquals("b-ccba", sb.toString());
        
        sb = new StrBuilder("abcba");
        sb.replaceFirst(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("axbxcba", sb.toString());
        
        sb = new StrBuilder("bb");
        sb.replaceFirst(StrMatcher.charMatcher('b'), "xbx");
        assertEquals("xbxb", sb.toString());
        
        sb = new StrBuilder("A1-A2A3-A4");
        sb.replaceFirst(A_NUMBER_MATCHER, "***");
        assertEquals("***-A2A3-A4", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryMatcher
    public void testReplace_StrMatcher_String_int_int_int_VaryMatcher() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replace((StrMatcher) null, "x", 0, sb.length(), -1);
        assertEquals("abcbccba", sb.toString());
        
        sb.replace(StrMatcher.charMatcher('a'), "x", 0, sb.length(), -1);
        assertEquals("xbcbccbx", sb.toString());
        
        sb.replace(StrMatcher.stringMatcher("cb"), "x", 0, sb.length(), -1);
        assertEquals("xbxcxx", sb.toString());
        
        sb = new StrBuilder("A1-A2A3-A4");
        sb.replace(A_NUMBER_MATCHER, "***", 0, sb.length(), -1);
        assertEquals("***-******-***", sb.toString());
        
        sb = new StrBuilder();
        sb.replace(A_NUMBER_MATCHER, "***", 0, sb.length(), -1);
        assertEquals("", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryReplace
    public void testReplace_StrMatcher_String_int_int_int_VaryReplace() {
        StrBuilder sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "cb", 0, sb.length(), -1);
        assertEquals("abcbccba", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "-", 0, sb.length(), -1);
        assertEquals("ab-c-a", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "+++", 0, sb.length(), -1);
        assertEquals("ab+++c+++a", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), "", 0, sb.length(), -1);
        assertEquals("abca", sb.toString());
        
        sb = new StrBuilder("abcbccba");
        sb.replace(StrMatcher.stringMatcher("cb"), null, 0, sb.length(), -1);
        assertEquals("abca", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryStartIndex
    public void testReplace_StrMatcher_String_int_int_int_VaryStartIndex() {
        StrBuilder sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, sb.length(), -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 1, sb.length(), -1);
        assertEquals("aax--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 2, sb.length(), -1);
        assertEquals("aax--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 3, sb.length(), -1);
        assertEquals("aax--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 4, sb.length(), -1);
        assertEquals("aaxa-ay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 5, sb.length(), -1);
        assertEquals("aaxaa-y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 6, sb.length(), -1);
        assertEquals("aaxaaaay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 7, sb.length(), -1);
        assertEquals("aaxaaaay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 8, sb.length(), -1);
        assertEquals("aaxaaaay-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 9, sb.length(), -1);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 10, sb.length(), -1);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        try {
            sb.replace(StrMatcher.stringMatcher("aa"), "-", 11, sb.length(), -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        try {
            sb.replace(StrMatcher.stringMatcher("aa"), "-", -1, sb.length(), -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        assertEquals("aaxaaaayaa", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryEndIndex
    public void testReplace_StrMatcher_String_int_int_int_VaryEndIndex() {
        StrBuilder sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 0, -1);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 2, -1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 3, -1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 4, -1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 5, -1);
        assertEquals("-x-aayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 6, -1);
        assertEquals("-x-aayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 7, -1);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 8, -1);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 9, -1);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 1000, -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        try {
            sb.replace(StrMatcher.stringMatcher("aa"), "-", 2, 1, -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        assertEquals("aaxaaaayaa", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReplace_StrMatcher_String_int_int_int_VaryCount
    public void testReplace_StrMatcher_String_int_int_int_VaryCount() {
        StrBuilder sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, -1);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 0);
        assertEquals("aaxaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 1);
        assertEquals("-xaaaayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 2);
        assertEquals("-x-aayaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 3);
        assertEquals("-x--yaa", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 4);
        assertEquals("-x--y-", sb.toString());
        
        sb = new StrBuilder("aaxaaaayaa");
        sb.replace(StrMatcher.stringMatcher("aa"), "-", 0, 10, 5);
        assertEquals("-x--y-", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testReverse
    public void testReverse() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.reverse().toString());
        
        sb.clear().append(true);
        assertEquals("eurt", sb.reverse().toString());
        assertEquals("true", sb.reverse().toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testTrim
    public void testTrim() {
        StrBuilder sb = new StrBuilder();
        assertEquals("", sb.reverse().toString());
        
        sb.clear().append(" \u0000 ");
        assertEquals("", sb.trim().toString());
        
        sb.clear().append(" \u0000 a b c");
        assertEquals("a b c", sb.trim().toString());
        
        sb.clear().append("a b c \u0000 ");
        assertEquals("a b c", sb.trim().toString());
        
        sb.clear().append(" \u0000 a b c \u0000 ");
        assertEquals("a b c", sb.trim().toString());
        
        sb.clear().append("a b c");
        assertEquals("a b c", sb.trim().toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testStartsWith
    public void testStartsWith() {
        StrBuilder sb = new StrBuilder();
        assertFalse(sb.startsWith("a"));
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));
        sb.append("abc");
        assertTrue(sb.startsWith("a"));
        assertTrue(sb.startsWith("ab"));
        assertTrue(sb.startsWith("abc"));
        assertFalse(sb.startsWith("cba"));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEndsWith
    public void testEndsWith() {
        StrBuilder sb = new StrBuilder();
        assertFalse(sb.endsWith("a"));
        assertFalse(sb.endsWith("c"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith(null));
        sb.append("abc");
        assertTrue(sb.endsWith("c"));
        assertTrue(sb.endsWith("bc"));
        assertTrue(sb.endsWith("abc"));
        assertFalse(sb.endsWith("cba"));
        assertFalse(sb.endsWith("abcd"));
        assertFalse(sb.endsWith(" abc"));
        assertFalse(sb.endsWith("abc "));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSubSequenceIntInt
    public void testSubSequenceIntInt() {
       StrBuilder sb = new StrBuilder ("hello goodbye");
       
       try {
            sb.subSequence(-1, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
       try {
            sb.subSequence(2, -1);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
        try {
            sb.subSequence(2, sb.length() + 1);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
        try {
            sb.subSequence(3, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        
        assertEquals ("hello", sb.subSequence(0, 5));
        assertEquals ("hello goodbye".subSequence(0, 6), sb.subSequence(0, 6));
        assertEquals ("goodbye", sb.subSequence(6, 13));
        assertEquals ("hello goodbye".subSequence(6,13), sb.subSequence(6, 13));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSubstringInt
    public void testSubstringInt() {
        StrBuilder sb = new StrBuilder ("hello goodbye");
        assertEquals ("goodbye", sb.substring(6));
        assertEquals ("hello goodbye".substring(6), sb.substring(6));
        assertEquals ("hello goodbye", sb.substring(0));
        assertEquals ("hello goodbye".substring(0), sb.substring(0));
        try {
            sb.substring(-1);
            fail ();
        } catch (IndexOutOfBoundsException e) {}
        
        try {
            sb.substring(15);
            fail ();
        } catch (IndexOutOfBoundsException e) {}
    
    }

// org.apache.commons.lang3.text.StrBuilderTest::testSubstringIntInt
    public void testSubstringIntInt() {
        StrBuilder sb = new StrBuilder ("hello goodbye");
        assertEquals ("hello", sb.substring(0, 5));
        assertEquals ("hello goodbye".substring(0, 6), sb.substring(0, 6));
        
        assertEquals ("goodbye", sb.substring(6, 13));
        assertEquals ("hello goodbye".substring(6,13), sb.substring(6, 13));
        
        assertEquals ("goodbye", sb.substring(6, 20));
        
        try {
            sb.substring(-1, 5);
            fail();
        } catch (IndexOutOfBoundsException e) {}
        
        try {
            sb.substring(15, 20);
            fail();
        } catch (IndexOutOfBoundsException e) {}
    }

// org.apache.commons.lang3.text.StrBuilderTest::testMidString
    public void testMidString() {
        StrBuilder sb = new StrBuilder("hello goodbye hello");
        assertEquals("goodbye", sb.midString(6, 7));
        assertEquals("hello", sb.midString(0, 5));
        assertEquals("hello", sb.midString(-5, 5));
        assertEquals("", sb.midString(0, -1));
        assertEquals("", sb.midString(20, 2));
        assertEquals("hello", sb.midString(14, 22));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testRightString
    public void testRightString() {
        StrBuilder sb = new StrBuilder("left right");
        assertEquals("right", sb.rightString(5));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.rightString(-5));
        assertEquals("left right", sb.rightString(15));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLeftString
    public void testLeftString() {
        StrBuilder sb = new StrBuilder("left right");
        assertEquals("left", sb.leftString(4));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.leftString(-5));
        assertEquals("left right", sb.leftString(15));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testContains_char
    public void testContains_char() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains('a'));
        assertEquals(true, sb.contains('o'));
        assertEquals(true, sb.contains('z'));
        assertEquals(false, sb.contains('1'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testContains_String
    public void testContains_String() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains("a"));
        assertEquals(true, sb.contains("pq"));
        assertEquals(true, sb.contains("z"));
        assertEquals(false, sb.contains("zyx"));
        assertEquals(false, sb.contains((String) null));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testContains_StrMatcher
    public void testContains_StrMatcher() {
        StrBuilder sb = new StrBuilder("abcdefghijklmnopqrstuvwxyz");
        assertEquals(true, sb.contains(StrMatcher.charMatcher('a')));
        assertEquals(true, sb.contains(StrMatcher.stringMatcher("pq")));
        assertEquals(true, sb.contains(StrMatcher.charMatcher('z')));
        assertEquals(false, sb.contains(StrMatcher.stringMatcher("zy")));
        assertEquals(false, sb.contains((StrMatcher) null));

        sb = new StrBuilder();
        assertEquals(false, sb.contains(A_NUMBER_MATCHER));
        sb.append("B A1 C");
        assertEquals(true, sb.contains(A_NUMBER_MATCHER));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_char
    public void testIndexOf_char() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(0, sb.indexOf('a'));
        
        
        assertEquals("abab".indexOf('a'), sb.indexOf('a'));

        assertEquals(1, sb.indexOf('b'));
        assertEquals("abab".indexOf('b'), sb.indexOf('b'));

        assertEquals(-1, sb.indexOf('z'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_char_int
    public void testIndexOf_char_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(0, sb.indexOf('a', -1));
        assertEquals(0, sb.indexOf('a', 0));
        assertEquals(2, sb.indexOf('a', 1));
        assertEquals(-1, sb.indexOf('a', 4));
        assertEquals(-1, sb.indexOf('a', 5));

        
        assertEquals("abab".indexOf('a', 1), sb.indexOf('a', 1));

        assertEquals(3, sb.indexOf('b', 2));
        assertEquals("abab".indexOf('b', 2), sb.indexOf('b', 2));

        assertEquals(-1, sb.indexOf('z', 2));

        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.indexOf('z', 0));
        assertEquals(-1, sb.indexOf('z', 3));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_char
    public void testLastIndexOf_char() {
        StrBuilder sb = new StrBuilder("abab");
        
        assertEquals (2, sb.lastIndexOf('a'));
        
        assertEquals ("abab".lastIndexOf('a'), sb.lastIndexOf('a'));
        
        assertEquals(3, sb.lastIndexOf('b'));
        assertEquals ("abab".lastIndexOf('b'), sb.lastIndexOf('b'));
        
        assertEquals (-1, sb.lastIndexOf('z'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_char_int
    public void testLastIndexOf_char_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(-1, sb.lastIndexOf('a', -1));
        assertEquals(0, sb.lastIndexOf('a', 0));
        assertEquals(0, sb.lastIndexOf('a', 1));

        
        assertEquals("abab".lastIndexOf('a', 1), sb.lastIndexOf('a', 1));

        assertEquals(1, sb.lastIndexOf('b', 2));
        assertEquals("abab".lastIndexOf('b', 2), sb.lastIndexOf('b', 2));

        assertEquals(-1, sb.lastIndexOf('z', 2));

        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.lastIndexOf('z', sb.length()));
        assertEquals(-1, sb.lastIndexOf('z', 1));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_String
    public void testIndexOf_String() {
        StrBuilder sb = new StrBuilder("abab");
        
        assertEquals(0, sb.indexOf("a"));
        
        assertEquals("abab".indexOf("a"), sb.indexOf("a"));
        
        assertEquals(0, sb.indexOf("ab"));
        
        assertEquals("abab".indexOf("ab"), sb.indexOf("ab"));
        
        assertEquals(1, sb.indexOf("b"));
        assertEquals("abab".indexOf("b"), sb.indexOf("b"));
        
        assertEquals(1, sb.indexOf("ba"));
        assertEquals("abab".indexOf("ba"), sb.indexOf("ba"));
        
        assertEquals(-1, sb.indexOf("z"));
        
        assertEquals(-1, sb.indexOf((String) null));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_String_int
    public void testIndexOf_String_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(0, sb.indexOf("a", -1));
        assertEquals(0, sb.indexOf("a", 0));
        assertEquals(2, sb.indexOf("a", 1));
        assertEquals(2, sb.indexOf("a", 2));
        assertEquals(-1, sb.indexOf("a", 3));
        assertEquals(-1, sb.indexOf("a", 4));
        assertEquals(-1, sb.indexOf("a", 5));
        
        assertEquals(-1, sb.indexOf("abcdef", 0));
        assertEquals(0, sb.indexOf("", 0));
        assertEquals(1, sb.indexOf("", 1));
        
        
        assertEquals ("abab".indexOf("a", 1), sb.indexOf("a", 1));
        
        assertEquals(2, sb.indexOf("ab", 1));
        
        assertEquals("abab".indexOf("ab", 1), sb.indexOf("ab", 1));
        
        assertEquals(3, sb.indexOf("b", 2));
        assertEquals("abab".indexOf("b", 2), sb.indexOf("b", 2));
        
        assertEquals(1, sb.indexOf("ba", 1));
        assertEquals("abab".indexOf("ba", 2), sb.indexOf("ba", 2));
        
        assertEquals(-1, sb.indexOf("z", 2));
        
        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.indexOf("za", 0));
        assertEquals(-1, sb.indexOf("za", 3));
        
        assertEquals(-1, sb.indexOf((String) null, 2));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_String
    public void testLastIndexOf_String() {
        StrBuilder sb = new StrBuilder("abab");
        
        assertEquals(2, sb.lastIndexOf("a"));
        
        assertEquals("abab".lastIndexOf("a"), sb.lastIndexOf("a"));
        
        assertEquals(2, sb.lastIndexOf("ab"));
        
        assertEquals("abab".lastIndexOf("ab"), sb.lastIndexOf("ab"));
        
        assertEquals(3, sb.lastIndexOf("b"));
        assertEquals("abab".lastIndexOf("b"), sb.lastIndexOf("b"));
        
        assertEquals(1, sb.lastIndexOf("ba"));
        assertEquals("abab".lastIndexOf("ba"), sb.lastIndexOf("ba"));
        
        assertEquals(-1, sb.lastIndexOf("z"));
        
        assertEquals(-1, sb.lastIndexOf((String) null));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_String_int
    public void testLastIndexOf_String_int() {
        StrBuilder sb = new StrBuilder("abab");
        assertEquals(-1, sb.lastIndexOf("a", -1));
        assertEquals(0, sb.lastIndexOf("a", 0));
        assertEquals(0, sb.lastIndexOf("a", 1));
        assertEquals(2, sb.lastIndexOf("a", 2));
        assertEquals(2, sb.lastIndexOf("a", 3));
        assertEquals(2, sb.lastIndexOf("a", 4));
        assertEquals(2, sb.lastIndexOf("a", 5));
        
        assertEquals(-1, sb.lastIndexOf("abcdef", 3));
        assertEquals("abab".lastIndexOf("", 3), sb.lastIndexOf("", 3));
        assertEquals("abab".lastIndexOf("", 1), sb.lastIndexOf("", 1));
        
        
        assertEquals("abab".lastIndexOf("a", 1), sb.lastIndexOf("a", 1));
        
        assertEquals(0, sb.lastIndexOf("ab", 1));
        
        assertEquals("abab".lastIndexOf("ab", 1), sb.lastIndexOf("ab", 1));
        
        assertEquals(1, sb.lastIndexOf("b", 2));
        assertEquals("abab".lastIndexOf("b", 2), sb.lastIndexOf("b", 2));
        
        assertEquals(1, sb.lastIndexOf("ba", 2));
        assertEquals("abab".lastIndexOf("ba", 2), sb.lastIndexOf("ba", 2));
        
        assertEquals(-1, sb.lastIndexOf("z", 2));
        
        sb = new StrBuilder("xyzabc");
        assertEquals(2, sb.lastIndexOf("za", sb.length()));
        assertEquals(-1, sb.lastIndexOf("za", 1));
        
        assertEquals(-1, sb.lastIndexOf((String) null, 2));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_StrMatcher
    public void testIndexOf_StrMatcher() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.indexOf((StrMatcher) null));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a')));
        
        sb.append("ab bd");
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('a')));
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b')));
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher()));
        assertEquals(4, sb.indexOf(StrMatcher.charMatcher('d')));
        assertEquals(-1, sb.indexOf(StrMatcher.noneMatcher()));
        assertEquals(-1, sb.indexOf((StrMatcher) null));
        
        sb.append(" A1 junction");
        assertEquals(6, sb.indexOf(A_NUMBER_MATCHER));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOf_StrMatcher_int
    public void testIndexOf_StrMatcher_int() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.indexOf((StrMatcher) null, 2));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 0));
        
        sb.append("ab bd");
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('a'), -2));
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('a'), 0));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 20));
        
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b'), -1));
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b'), 0));
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('b'), 1));
        assertEquals(3, sb.indexOf(StrMatcher.charMatcher('b'), 2));
        assertEquals(3, sb.indexOf(StrMatcher.charMatcher('b'), 3));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('b'), 4));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('b'), 5));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('b'), 6));
        
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher(), -2));
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher(), 0));
        assertEquals(2, sb.indexOf(StrMatcher.spaceMatcher(), 2));
        assertEquals(-1, sb.indexOf(StrMatcher.spaceMatcher(), 4));
        assertEquals(-1, sb.indexOf(StrMatcher.spaceMatcher(), 20));
        
        assertEquals(-1, sb.indexOf(StrMatcher.noneMatcher(), 0));
        assertEquals(-1, sb.indexOf((StrMatcher) null, 0));
        
        sb.append(" A1 junction with A2");
        assertEquals(6, sb.indexOf(A_NUMBER_MATCHER, 5));
        assertEquals(6, sb.indexOf(A_NUMBER_MATCHER, 6));
        assertEquals(23, sb.indexOf(A_NUMBER_MATCHER, 7));
        assertEquals(23, sb.indexOf(A_NUMBER_MATCHER, 22));
        assertEquals(23, sb.indexOf(A_NUMBER_MATCHER, 23));
        assertEquals(-1, sb.indexOf(A_NUMBER_MATCHER, 24));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_StrMatcher
    public void testLastIndexOf_StrMatcher() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a')));
        
        sb.append("ab bd");
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a')));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b')));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher()));
        assertEquals(4, sb.lastIndexOf(StrMatcher.charMatcher('d')));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.noneMatcher()));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        
        sb.append(" A1 junction");
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLastIndexOf_StrMatcher_int
    public void testLastIndexOf_StrMatcher_int() {
        StrBuilder sb = new StrBuilder();
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null, 2));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), 0));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), -1));
        
        sb.append("ab bd");
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), -2));
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a'), 0));
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a'), 2));
        assertEquals(0, sb.lastIndexOf(StrMatcher.charMatcher('a'), 20));
        
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('b'), -1));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('b'), 0));
        assertEquals(1, sb.lastIndexOf(StrMatcher.charMatcher('b'), 1));
        assertEquals(1, sb.lastIndexOf(StrMatcher.charMatcher('b'), 2));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 3));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 4));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 5));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('b'), 6));
        
        assertEquals(-1, sb.lastIndexOf(StrMatcher.spaceMatcher(), -2));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.spaceMatcher(), 0));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher(), 2));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher(), 4));
        assertEquals(2, sb.lastIndexOf(StrMatcher.spaceMatcher(), 20));
        
        assertEquals(-1, sb.lastIndexOf(StrMatcher.noneMatcher(), 0));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null, 0));
        
        sb.append(" A1 junction with A2");
        assertEquals(-1, sb.lastIndexOf(A_NUMBER_MATCHER, 5));
        assertEquals(-1, sb.lastIndexOf(A_NUMBER_MATCHER, 6)); 
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER, 7));
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER, 22));
        assertEquals(6, sb.lastIndexOf(A_NUMBER_MATCHER, 23)); 
        assertEquals(23, sb.lastIndexOf(A_NUMBER_MATCHER, 24));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testAsTokenizer
    public void testAsTokenizer() throws Exception {
        
        StrBuilder b = new StrBuilder();
        b.append("a b ");
        StrTokenizer t = b.asTokenizer();
        
        String[] tokens1 = t.getTokenArray();
        assertEquals(2, tokens1.length);
        assertEquals("a", tokens1[0]);
        assertEquals("b", tokens1[1]);
        assertEquals(2, t.size());
        
        b.append("c d ");
        String[] tokens2 = t.getTokenArray();
        assertEquals(2, tokens2.length);
        assertEquals("a", tokens2[0]);
        assertEquals("b", tokens2[1]);
        assertEquals(2, t.size());
        assertEquals("a", t.next());
        assertEquals("b", t.next());
        
        t.reset();
        String[] tokens3 = t.getTokenArray();
        assertEquals(4, tokens3.length);
        assertEquals("a", tokens3[0]);
        assertEquals("b", tokens3[1]);
        assertEquals("c", tokens3[2]);
        assertEquals("d", tokens3[3]);
        assertEquals(4, t.size());
        assertEquals("a", t.next());
        assertEquals("b", t.next());
        assertEquals("c", t.next());
        assertEquals("d", t.next());
        
        assertEquals("a b c d ", t.getContent());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testAsReader
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("some text");
        Reader reader = sb.asReader();
        assertEquals(true, reader.ready());
        char[] buf = new char[40];
        assertEquals(9, reader.read(buf));
        assertEquals("some text", new String(buf, 0, 9));
        
        assertEquals(-1, reader.read());
        assertEquals(false, reader.ready());
        assertEquals(0, reader.skip(2));
        assertEquals(0, reader.skip(-1));
        
        assertEquals(true, reader.markSupported());
        reader = sb.asReader();
        assertEquals('s', reader.read());
        reader.mark(-1);
        char[] array = new char[3];
        assertEquals(3, reader.read(array, 0, 3));
        assertEquals('o', array[0]);
        assertEquals('m', array[1]);
        assertEquals('e', array[2]);
        reader.reset();
        assertEquals(1, reader.read(array, 1, 1));
        assertEquals('o', array[0]);
        assertEquals('o', array[1]);
        assertEquals('e', array[2]);
        assertEquals(2, reader.skip(2));
        assertEquals(' ', reader.read());
        
        assertEquals(true, reader.ready());
        reader.close();
        assertEquals(true, reader.ready());
        
        reader = sb.asReader();
        array = new char[3];
        try {
            reader.read(array, -1, 0);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, 100, 1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, 0, 100);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            reader.read(array, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        
        assertEquals(0, reader.read(array, 0, 0));
        assertEquals(0, array[0]);
        assertEquals(0, array[1]);
        assertEquals(0, array[2]);
        
        reader.skip(9);
        assertEquals(-1, reader.read(array, 0, 1));
        
        reader.reset();
        array = new char[30];
        assertEquals(9, reader.read(array, 0, 30));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testAsWriter
    public void testAsWriter() throws Exception {
        StrBuilder sb = new StrBuilder("base");
        Writer writer = sb.asWriter();
        
        writer.write('l');
        assertEquals("basel", sb.toString());
        
        writer.write(new char[] {'i', 'n'});
        assertEquals("baselin", sb.toString());
        
        writer.write(new char[] {'n', 'e', 'r'}, 1, 2);
        assertEquals("baseliner", sb.toString());
        
        writer.write(" rout");
        assertEquals("baseliner rout", sb.toString());
        
        writer.write("ping that server", 1, 3);
        assertEquals("baseliner routing", sb.toString());
        
        writer.flush();  
        assertEquals("baseliner routing", sb.toString());
        
        writer.close();  
        assertEquals("baseliner routing", sb.toString());
        
        writer.write(" hi");  
        assertEquals("baseliner routing hi", sb.toString());
        
        sb.setLength(4);  
        writer.write('d');
        assertEquals("based", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEqualsIgnoreCase
    public void testEqualsIgnoreCase() {
        StrBuilder sb1 = new StrBuilder();
        StrBuilder sb2 = new StrBuilder();
        assertEquals(true, sb1.equalsIgnoreCase(sb1));
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
        assertEquals(true, sb2.equalsIgnoreCase(sb2));
        
        sb1.append("abc");
        assertEquals(false, sb1.equalsIgnoreCase(sb2));
        
        sb2.append("ABC");
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
        
        sb2.clear().append("abc");
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
        assertEquals(true, sb1.equalsIgnoreCase(sb1));
        assertEquals(true, sb2.equalsIgnoreCase(sb2));
        
        sb2.clear().append("aBc");
        assertEquals(true, sb1.equalsIgnoreCase(sb2));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testEquals
    public void testEquals() {
        StrBuilder sb1 = new StrBuilder();
        StrBuilder sb2 = new StrBuilder();
        assertEquals(true, sb1.equals(sb2));
        assertEquals(true, sb1.equals(sb1));
        assertEquals(true, sb2.equals(sb2));
        assertEquals(true, sb1.equals((Object) sb2));
        
        sb1.append("abc");
        assertEquals(false, sb1.equals(sb2));
        assertEquals(false, sb1.equals((Object) sb2));
        
        sb2.append("ABC");
        assertEquals(false, sb1.equals(sb2));
        assertEquals(false, sb1.equals((Object) sb2));
        
        sb2.clear().append("abc");
        assertEquals(true, sb1.equals(sb2));
        assertEquals(true, sb1.equals((Object) sb2));
        
        assertEquals(false, sb1.equals(new Integer(1)));
        assertEquals(false, sb1.equals("abc"));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testHashCode
    public void testHashCode() {
        StrBuilder sb = new StrBuilder();
        int hc1a = sb.hashCode();
        int hc1b = sb.hashCode();
        assertEquals(0, hc1a);
        assertEquals(hc1a, hc1b);
        
        sb.append("abc");
        int hc2a = sb.hashCode();
        int hc2b = sb.hashCode();
        assertEquals(true, hc2a != 0);
        assertEquals(hc2a, hc2b);
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToString
    public void testToString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("abc", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testToStringBuffer
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder();
        assertEquals(new StringBuffer().toString(), sb.toStringBuffer().toString());
        
        sb.append("junit");
        assertEquals(new StringBuffer("junit").toString(), sb.toStringBuffer().toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang294
    public void testLang294() {
        StrBuilder sb = new StrBuilder("\n%BLAH%\nDo more stuff\neven more stuff\n%BLAH%\n");
        sb.deleteAll("\n%BLAH%");
        assertEquals("\nDo more stuff\neven more stuff\n", sb.toString()); 
    }

// org.apache.commons.lang3.text.StrBuilderTest::testIndexOfLang294
    public void testIndexOfLang294() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.deleteFirst("three");
        assertEquals(-1, sb.indexOf("three"));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang295
    public void testLang295() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.deleteFirst("three");
        assertFalse( "The contains(char) method is looking beyond the end of the string", sb.contains('h'));
        assertEquals( "The indexOf(char) method is looking beyond the end of the string", -1, sb.indexOf('h'));
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang412Right
    public void testLang412Right() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadRight correctly", "**********", sb.toString());
    }

// org.apache.commons.lang3.text.StrBuilderTest::testLang412Left
    public void testLang412Left() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 10, '*');
        assertEquals( "Failed to invoke appendFixedWidthPadLeft correctly", "**********", sb.toString());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::test1
    public void test1() {

        String input = "a;b;c;\"d;\"\"e\";f; ; ;  ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f", "", "", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test2
    public void test2() {

        String input = "a;b;c ;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c ", "d;\"e", "f", " ", " ", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test3
    public void test3() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", " c", "d;\"e", "f", " ", " ", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test4
    public void test4() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test5
    public void test5() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d;\"e", "f", null, null, null,};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test6
    public void test6() {

        String input = "a;b; c;\"d;\"\"e\";f; ; ;";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterChar(';');
        tok.setQuoteChar('"');
        tok.setIgnoredMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", " c", "d;\"e", "f", null, null, null,};

        int nextCount = 0;
        while (tok.hasNext()) {
            tok.next();
            nextCount++;
        }

        int prevCount = 0;
        while (tok.hasPrevious()) {
            tok.previous();
            prevCount++;
        }

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);

        assertTrue("could not cycle through entire token list" + " using the 'hasNext' and 'next' methods",
                nextCount == expected.length);

        assertTrue("could not cycle through entire token list" + " using the 'hasPrevious' and 'previous' methods",
                prevCount == expected.length);

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test7
    public void test7() {

        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterMatcher(StrMatcher.spaceMatcher());
        tok.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(false);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "", "", "b", "c", "d e", "f", "",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::test8
    public void test8() {

        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setDelimiterMatcher(StrMatcher.spaceMatcher());
        tok.setQuoteMatcher(StrMatcher.doubleQuoteMatcher());
        tok.setIgnoredMatcher(StrMatcher.noneMatcher());
        tok.setIgnoreEmptyTokens(true);
        String tokens[] = tok.getTokenArray();

        String expected[] = new String[]{"a", "b", "c", "d e", "f",};

        assertEquals(ArrayUtils.toString(tokens), expected.length, tokens.length);
        for (int i = 0; i < expected.length; i++) {
            assertTrue("token[" + i + "] was '" + tokens[i] + "' but was expected to be '" + expected[i] + "'",
                    ObjectUtils.equals(expected[i], tokens[i]));
        }

    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic1
    public void testBasic1() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic2
    public void testBasic2() {
        String input = "a \nb\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic3
    public void testBasic3() {
        String input = "a \nb\u0001\fc";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("b\u0001", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic4
    public void testBasic4() {
        String input = "a \"b\" c";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals("a", tok.next());
        assertEquals("\"b\"", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasic5
    public void testBasic5() {
        String input = "a:b':c";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        assertEquals("a", tok.next());
        assertEquals("b'", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicDelim1
    public void testBasicDelim1() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ':');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicDelim2
    public void testBasicDelim2() {
        String input = "a:b:c";
        StrTokenizer tok = new StrTokenizer(input, ',');
        assertEquals("a:b:c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicEmpty1
    public void testBasicEmpty1() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setIgnoreEmptyTokens(false);
        assertEquals("a", tok.next());
        assertEquals("", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicEmpty2
    public void testBasicEmpty2() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals(null, tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted1
    public void testBasicQuoted1() {
        String input = "a 'b' c";
        StrTokenizer tok = new StrTokenizer(input, ' ', '\'');
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted2
    public void testBasicQuoted2() {
        String input = "a:'b':";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted3
    public void testBasicQuoted3() {
        String input = "a:'b''c'";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b'c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted4
    public void testBasicQuoted4() {
        String input = "a: 'b' 'c' :d";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b c", tok.next());
        assertEquals("d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted5
    public void testBasicQuoted5() {
        String input = "a: 'b'x'c' :d";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bxc", tok.next());
        assertEquals("d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted6
    public void testBasicQuoted6() {
        String input = "a:'b'\"c':d";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("b\"c:d", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuoted7
    public void testBasicQuoted7() {
        String input = "a:\"There's a reason here\":b";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setQuoteMatcher(StrMatcher.quoteMatcher());
        assertEquals("a", tok.next());
        assertEquals("There's a reason here", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicQuotedTrimmed1
    public void testBasicQuotedTrimmed1() {
        String input = "a: 'b' :";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicTrimmed1
    public void testBasicTrimmed1() {
        String input = "a: b :  ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicTrimmed2
    public void testBasicTrimmed2() {
        String input = "a:  b  :";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setTrimmerMatcher(StrMatcher.stringMatcher("  "));
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed1
    public void testBasicIgnoreTrimmed1() {
        String input = "a: bIGNOREc : ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bc", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed2
    public void testBasicIgnoreTrimmed2() {
        String input = "IGNOREaIGNORE: IGNORE bIGNOREc IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bc", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed3
    public void testBasicIgnoreTrimmed3() {
        String input = "IGNOREaIGNORE: IGNORE bIGNOREc IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("  bc  ", tok.next());
        assertEquals("  ", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testBasicIgnoreTrimmed4
    public void testBasicIgnoreTrimmed4() {
        String input = "IGNOREaIGNORE: IGNORE 'bIGNOREc'IGNORE'd' IGNORE : IGNORE ";
        StrTokenizer tok = new StrTokenizer(input, ':', '\'');
        tok.setIgnoredMatcher(StrMatcher.stringMatcher("IGNORE"));
        tok.setTrimmerMatcher(StrMatcher.trimMatcher());
        tok.setIgnoreEmptyTokens(false);
        tok.setEmptyTokenAsNull(true);
        assertEquals("a", tok.next());
        assertEquals("bIGNOREcd", tok.next());
        assertEquals(null, tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testListArray
    public void testListArray() {
        String input = "a  b c";
        StrTokenizer tok = new StrTokenizer(input);
        String[] array = tok.getTokenArray();
        List<?> list = tok.getTokenList();
        
        assertEquals(Arrays.asList(array), list);
        assertEquals(3, list.size());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSV
    public void testCSV(String data) {
        this.testXSVAbc(StrTokenizer.getCSVInstance(data));
        this.testXSVAbc(StrTokenizer.getCSVInstance(data.toCharArray()));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVEmpty
    public void testCSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVSimple
    public void testCSVSimple() {
        this.testCSV(CSV_SIMPLE_FIXTURE);
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCSVSimpleNeedsTrim
    public void testCSVSimpleNeedsTrim() {
        this.testCSV("   " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n\t  " + CSV_SIMPLE_FIXTURE);
        this.testCSV("   \n  " + CSV_SIMPLE_FIXTURE + "\n\n\r");
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testGetContent
    public void testGetContent() {
        String input = "a   b c \"d e\" f ";
        StrTokenizer tok = new StrTokenizer(input);
        assertEquals(input, tok.getContent());

        tok = new StrTokenizer(input.toCharArray());
        assertEquals(input, tok.getContent());
        
        tok = new StrTokenizer();
        assertEquals(null, tok.getContent());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testChaining
    public void testChaining() {
        StrTokenizer tok = new StrTokenizer();
        assertEquals(tok, tok.reset());
        assertEquals(tok, tok.reset(""));
        assertEquals(tok, tok.reset(new char[0]));
        assertEquals(tok, tok.setDelimiterChar(' '));
        assertEquals(tok, tok.setDelimiterString(" "));
        assertEquals(tok, tok.setDelimiterMatcher(null));
        assertEquals(tok, tok.setQuoteChar(' '));
        assertEquals(tok, tok.setQuoteMatcher(null));
        assertEquals(tok, tok.setIgnoredChar(' '));
        assertEquals(tok, tok.setIgnoredMatcher(null));
        assertEquals(tok, tok.setTrimmerMatcher(null));
        assertEquals(tok, tok.setEmptyTokenAsNull(false));
        assertEquals(tok, tok.setIgnoreEmptyTokens(false));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneNotSupportedException
    public void testCloneNotSupportedException() {
        Object notCloned = (new StrTokenizer() {
            @Override
            Object cloneReset() throws CloneNotSupportedException {
                throw new CloneNotSupportedException("test");
            }
        }).clone();
        assertNull(notCloned);
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneNull
    public void testCloneNull() {
        StrTokenizer tokenizer = new StrTokenizer((char[]) null);
        
        assertEquals(null, tokenizer.nextToken());
        tokenizer.reset();
        assertEquals(null, tokenizer.nextToken());
        
        StrTokenizer clonedTokenizer = (StrTokenizer) tokenizer.clone();
        tokenizer.reset();
        assertEquals(null, tokenizer.nextToken());
        assertEquals(null, clonedTokenizer.nextToken());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testCloneReset
    public void testCloneReset() {
        char[] input = new char[]{'a'};
        StrTokenizer tokenizer = new StrTokenizer(input);
        
        assertEquals("a", tokenizer.nextToken());
        tokenizer.reset(input);
        assertEquals("a", tokenizer.nextToken());
        
        StrTokenizer clonedTokenizer = (StrTokenizer) tokenizer.clone();
        input[0] = 'b';
        tokenizer.reset(input);
        assertEquals("b", tokenizer.nextToken());
        assertEquals("a", clonedTokenizer.nextToken());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String
    public void testConstructor_String() {
        StrTokenizer tok = new StrTokenizer("a b");
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("");
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String_char
    public void testConstructor_String_char() {
        StrTokenizer tok = new StrTokenizer("a b", ' ');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("", ' ');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null, ' ');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_String_char_char
    public void testConstructor_String_char_char() {
        StrTokenizer tok = new StrTokenizer("a b", ' ', '"');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals(1, tok.getQuoteMatcher().isMatch("\"".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer("", ' ', '"');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((String) null, ' ', '"');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray
    public void testConstructor_charArray() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray());
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0]);
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray_char
    public void testConstructor_charArray_char() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray(), ' ');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0], ' ');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null, ' ');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testConstructor_charArray_char_char
    public void testConstructor_charArray_char_char() {
        StrTokenizer tok = new StrTokenizer("a b".toCharArray(), ' ', '"');
        assertEquals(1, tok.getDelimiterMatcher().isMatch(" ".toCharArray(), 0, 0, 1));
        assertEquals(1, tok.getQuoteMatcher().isMatch("\"".toCharArray(), 0, 0, 1));
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer(new char[0], ' ', '"');
        assertEquals(false, tok.hasNext());
        
        tok = new StrTokenizer((char[]) null, ' ', '"');
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset
    public void testReset() {
        StrTokenizer tok = new StrTokenizer("a b c");
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset();
        assertEquals("a", tok.next());
        assertEquals("b", tok.next());
        assertEquals("c", tok.next());
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset_String
    public void testReset_String() {
        StrTokenizer tok = new StrTokenizer("x x x");
        tok.reset("d e");
        assertEquals("d", tok.next());
        assertEquals("e", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((String) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testReset_charArray
    public void testReset_charArray() {
        StrTokenizer tok = new StrTokenizer("x x x");
        
        char[] array = new char[] {'a', 'b', 'c'};
        tok.reset(array);
        assertEquals("abc", tok.next());
        assertEquals(false, tok.hasNext());
        
        tok.reset((char[]) null);
        assertEquals(false, tok.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTSV
    public void testTSV() {
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE));
        this.testXSVAbc(StrTokenizer.getTSVInstance(TSV_SIMPLE_FIXTURE.toCharArray()));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTSVEmpty
    public void testTSVEmpty() {
        this.testEmpty(StrTokenizer.getCSVInstance());
        this.testEmpty(StrTokenizer.getCSVInstance(""));
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testIteration
    public void testIteration() {
        StrTokenizer tkn = new StrTokenizer("a b c");
        assertEquals(false, tkn.hasPrevious());
        try {
            tkn.previous();
            fail();
        } catch (NoSuchElementException ex) {}
        assertEquals(true, tkn.hasNext());
        
        assertEquals("a", tkn.next());
        try {
            tkn.remove();
            fail();
        } catch (UnsupportedOperationException ex) {}
        try {
            tkn.set("x");
            fail();
        } catch (UnsupportedOperationException ex) {}
        try {
            tkn.add("y");
            fail();
        } catch (UnsupportedOperationException ex) {}
        assertEquals(true, tkn.hasPrevious());
        assertEquals(true, tkn.hasNext());
        
        assertEquals("b", tkn.next());
        assertEquals(true, tkn.hasPrevious());
        assertEquals(true, tkn.hasNext());
        
        assertEquals("c", tkn.next());
        assertEquals(true, tkn.hasPrevious());
        assertEquals(false, tkn.hasNext());
        
        try {
            tkn.next();
            fail();
        } catch (NoSuchElementException ex) {}
        assertEquals(true, tkn.hasPrevious());
        assertEquals(false, tkn.hasNext());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTokenizeSubclassInputChange
    public void testTokenizeSubclassInputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c d e") {
            @Override
            protected List<String> tokenize(char[] chars, int offset, int count) {
                return super.tokenize("w x y z".toCharArray(), 2, 5);
            }
        };
        assertEquals("x", tkn.next());
        assertEquals("y", tkn.next());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testTokenizeSubclassOutputChange
    public void testTokenizeSubclassOutputChange() {
        StrTokenizer tkn = new StrTokenizer("a b c") {
            @Override
            protected List<String> tokenize(char[] chars, int offset, int count) {
                List<String> list = super.tokenize(chars, offset, count);
                Collections.reverse(list);
                return list;
            }
        };
        assertEquals("c", tkn.next());
        assertEquals("b", tkn.next());
        assertEquals("a", tkn.next());
    }

// org.apache.commons.lang3.text.StrTokenizerTest::testToString
    public void testToString() {
        StrTokenizer tkn = new StrTokenizer("a b c d e");
        assertEquals("StrTokenizer[not tokenized yet]", tkn.toString());
        tkn.next();
        assertEquals("StrTokenizer[a, b, c, d, e]", tkn.toString());
    }
