public void writeBinary(Base64Variant b64variant,
    		byte[] data, int offset, int len) throws IOException
    {
        if (data == null) {
            writeNull();
            return;
        }
        _verifyValueWrite("write Binary value");
        try {
            if (_nextIsAttribute) {
                if (_nextName == null) {
                    handleMissingName();
                }
                // Stax2 API only has 'full buffer' write method:
                byte[] fullBuffer = toFullBuffer(data, offset, len);
                _xmlWriter.writeBinaryAttribute("", _nextName.getNamespaceURI(), _nextName.getLocalPart(), fullBuffer);
            } else if (checkNextIsUnwrapped()) {
            	// should we consider pretty-printing or not?
                _xmlWriter.writeBinary(data, offset, len);
            } else {
                if (_nextName == null) {
                    handleMissingName();
                }
                if (_xmlPrettyPrinter != null) {
                    _xmlPrettyPrinter.writeLeafElement(_xmlWriter,
                            _nextName.getNamespaceURI(), _nextName.getLocalPart(),
                            data, offset, len);
                } else {
                    if (len == 0) {
                        _xmlWriter.writeEmptyElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                    } else {
                        _xmlWriter.writeStartElement(_nextName.getNamespaceURI(), _nextName.getLocalPart());
                        _xmlWriter.writeBinary(data, offset, len);
                        _xmlWriter.writeEndElement();
                    }
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