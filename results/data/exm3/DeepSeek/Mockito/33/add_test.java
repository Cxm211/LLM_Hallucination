// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java
@Test
    public void shouldStubAndVerifyWithGenericSuperclass() {
        abstract class SuperClass<T> {
            public abstract T method(T arg);
        }
        class SubClass extends SuperClass<String> {
            @Override
            public String method(String arg) {
                return null;
            }
        }
        SubClass mock = Mockito.mock(SubClass.class);
        Mockito.when(mock.method("test")).thenReturn("result");
        Assert.assertEquals("result", mock.method("test"));
        Mockito.verify(mock).method("test");
        SuperClass raw = mock;
        Assert.assertEquals("result", raw.method("test"));
        Mockito.verify((SuperClass) mock).method("test");
    }
