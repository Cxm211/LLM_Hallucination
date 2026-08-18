// org/apache/commons/collections4/keyvalue/MultiKeyTest.java
public void testEqualsAfterSerializationWithSingleKey() throws IOException, ClassNotFoundException
{
    final MultiKey<String> mk = new MultiKey<>("A");

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream out = new ObjectOutputStream(baos);
    out.writeObject(mk);
    out.close();

    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final ObjectInputStream in = new ObjectInputStream(bais);
    final MultiKey<String> mk2 = (MultiKey<String>)in.readObject();
    in.close();

    assertEquals(mk.hashCode(), mk2.hashCode());
}
