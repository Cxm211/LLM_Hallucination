// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java
public void testWith6Bytes() throws Exception 
{
    String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C', 'D', 'E', 'F' ));
    assertEquals("<TestPojo><field>QUJDREVG</field></TestPojo>", xml);
}