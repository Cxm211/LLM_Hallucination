// org/apache/commons/lang3/reflect/TypeUtilsTest.java
public void dummyMethodForTest(List<? extends Integer> list1, List<? extends Number> list2) {}

    @Test
    public void testIsAssignableWildcardExtendsNumber() throws NoSuchMethodException {
        Method method = getClass().getMethod("dummyMethodForTest", List.class, List.class);
        Type[] types = method.getGenericParameterTypes();
        Assert.assertTrue(TypeUtils.isAssignable(types[0], types[1]));
    }
