// org/apache/commons/collections4/keyvalue/MultiKeyTest.java
public void testEqualsAfterSerializationWithNullKeys() throws IOException, ClassNotFoundException
{
    final MultiKey<Object> mk = new MultiKey<>(null, null);

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(mk);
    out.close();

    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final ObjectInputStream in = new ObjectInputStream(bais);
    final MultiKey<Object> mk2 = (MultiKey<Object>)in.readObject();
    in.close();

    assertEquals(mk.hashCode(), mk2.hashCode());
}
