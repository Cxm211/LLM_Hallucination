// org/mockito/internal/util/reflection/GenericMasterTest.java
@Test
    public void shouldDealWithTypeVariable() throws Exception {
        class GenericHolder<T> {
            T item;
        }
        Field field = GenericHolder.class.getDeclaredField("item");
        assertEquals(Object.class, m.getGenericType(field));
    }

    @Test
    public void shouldDealWithWildcard() throws Exception {
        class WildcardHolder {
            List<?> items;
        }
        Field field = WildcardHolder.class.getDeclaredField("items");
        assertEquals(Object.class, m.getGenericType(field));
    }

    @Test
    public void shouldDealWithGenericArray() throws Exception {
        class ArrayHolder {
            List<String[]> arrayList;
        }
        Field field = ArrayHolder.class.getDeclaredField("arrayList");
        assertEquals(Object.class, m.getGenericType(field));
    }
