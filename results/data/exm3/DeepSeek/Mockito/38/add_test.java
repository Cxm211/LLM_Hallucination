// org/mockito/internal/verification/argumentmatching/ArgumentMatchingToolTest.java
@Test
    public void shouldHandleToStringEqualsWithNullAndNonNull() throws Exception {
        java.lang.reflect.Method toStringEquals = org.mockito.internal.verification.argumentmatching.ArgumentMatchingTool.class.getDeclaredMethod("toStringEquals", org.hamcrest.Matcher.class, Object.class);
        toStringEquals.setAccessible(true);
        org.hamcrest.Matcher m = new org.mockito.internal.matchers.Equals(20);
        boolean result = (Boolean) toStringEquals.invoke(tool, m, null);
        org.junit.Assert.assertFalse(result);
        m = new org.mockito.internal.matchers.Equals("foo");
        result = (Boolean) toStringEquals.invoke(tool, m, "foo");
        org.junit.Assert.assertTrue(result);
        m = new org.mockito.internal.matchers.Equals("foo");
        result = (Boolean) toStringEquals.invoke(tool, m, "bar");
        org.junit.Assert.assertFalse(result);
    }
