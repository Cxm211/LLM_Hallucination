// buggy function
    public void writeBinary(Base64Variant b64variant,
    		byte[] data, int offset, int len) throws IOException
    {
        if (data == null) {
            writeNull();
            return;
        }
        _verifyValueWrite("write Binary value");
        if (_nextName == null) {
            handleMissingName();
        }
        try {
            if (_nextIsAttribute) {
                // Stax2 API only has 'full buffer' write method:
                byte[] fullBuffer = toFullBuffer(data, offset, len);
                _xmlWriter.writeBinaryAttribute("", _nextName.getNamespaceURI(), _nextName.getLocalPart(), fullBuffer);
            } else if (checkNextIsUnwrapped()) {
            	// should we consider pretty-printing or not?
                _xmlWriter.writeBinary(data, offset, len);
            } else {
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                            _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                            data, offset, len);
                } else {
                    _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                    _xmlWriter.writeBinary(data, offset, len);
                    _xmlWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException e) {
            StaxUtil.throwAsGenerationException(e, this);
        }
    }

    private byte[] toFullBuffer(byte[] data, int offset, int len)
    {
        // might already be ok:
        if (offset == 0 && len == data.length) {
            return data;
        }
        byte[] result = new byte[len];
        if (len > 0) {
            System.arraycopy(data, offset, result, 0, len);
        }
        return result;
    }

// trigger testcase
// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java::testWith0Bytes
public void testWith0Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo());
        assertEquals("<TestPojo><field/></TestPojo>", xml);
    }

// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java::testWith1Byte
public void testWith1Byte() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A' ));
        assertEquals("<TestPojo><field>QQ==</field></TestPojo>", xml);
    }

// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java::testWith2Bytes
public void testWith2Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B' ));
        assertEquals("<TestPojo><field>QUI=</field></TestPojo>", xml);
    }

// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java::testWith3Bytes
public void testWith3Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C' ));
        assertEquals("<TestPojo><field>QUJD</field></TestPojo>", xml);
    }

// com/fasterxml/jackson/dataformat/xml/ser/TestBinaryStreamToXMLSerialization.java::testWith4Bytes
public void testWith4Bytes() throws Exception 
    {
        String xml = MAPPER.writeValueAsString(createPojo( 'A', 'B', 'C', 'D' ));
        assertEquals("<TestPojo><field>QUJDRA==</field></TestPojo>", xml);
    }
