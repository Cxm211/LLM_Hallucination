// org/apache/commons/lang3/reflect/TypeUtilsTest.java
@Test
public void testGetTypeArgumentsWithTypeParameters() {
    Map<TypeVariable<?>, Type> typeVarAssigns;

    // Test with a class that has type parameters before reaching target
    typeVarAssigns = TypeUtils.getTypeArguments(ArrayList.class, List.class);
    Assert.assertNotNull("Type var assigns for List from ArrayList should not be null", typeVarAssigns);
    Assert.assertTrue("Type var assigns for List from ArrayList should contain the type variable",
            typeVarAssigns.containsKey(List.class.getTypeParameters()[0]));

    // Test with TreeSet which has type parameters
    typeVarAssigns = TypeUtils.getTypeArguments(TreeSet.class, Set.class);
    Assert.assertNotNull("Type var assigns for Set from TreeSet should not be null", typeVarAssigns);
    Assert.assertTrue("Type var assigns for Set from TreeSet should contain the type variable",
            typeVarAssigns.containsKey(Set.class.getTypeParameters()[0]));
}