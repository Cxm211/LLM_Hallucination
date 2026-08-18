// org/mockitousage/basicapi/MocksSerializationTest.java
@Test
    public void shouldBeSerializableWithMultipleExtraInterfaces() throws Exception {
        //when
        IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(List.class, Runnable.class).serializable());
        //then
        serializeAndBack((List) mock);
        serializeAndBack((Runnable) mock);
    }
