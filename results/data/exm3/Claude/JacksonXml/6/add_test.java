// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java
public void testWith5Bytes() throws Exception 
{
    String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C', 'D', 'E' ));
    assertEquals("<TestPojo><field>QUJDREU=</field></TestPojo>", xml);
}