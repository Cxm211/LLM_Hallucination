// org/mockitousage/bugs/InheritedGenericsPolimorphicCallTest.java
@Test
    public void shouldStubAndVerifyWithGenericInterface() {
        interface GenericInterface<T> {
            T getValue();
        }
        class ConcreteClass implements GenericInterface<String> {
            @Override
            public String getValue() {
                return null;
            }
        }
        ConcreteClass mock = Mockito.mock(ConcreteClass.class);
        Mockito.when(mock.getValue()).thenReturn("value");
        Assert.assertEquals("value", mock.getValue());
        Mockito.verify(mock).getValue();
        GenericInterface raw = mock;
        Assert.assertEquals("value", raw.getValue());
        Mockito.verify((GenericInterface) mock).getValue();
    }
