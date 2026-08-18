// org/apache/commons/lang3/reflect/TypeUtilsTest.java::testGetTypeArguments
@Test
public void testGetTypeArguments_additional() {
    Map<TypeVariable<?>, Type> typeVarAssigns;
    // Ensure mappings are collected across a generic subclass to its generic superclass
    typeVarAssigns = TypeUtils.getTypeArguments(AAAClass.BBBClass.class, AAClass.class);
    Assert.assertNotNull(typeVarAssigns);
    Assert.assertEquals(1, typeVarAssigns.size());
    Assert.assertEquals(String.class, typeVarAssigns.get(AAClass.class.getTypeParameters()[0]));
}