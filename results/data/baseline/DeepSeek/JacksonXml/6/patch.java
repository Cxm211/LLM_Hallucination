public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {
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
            byte[] fullBuffer = toFullBuffer(data, offset, len);
            _xmlWriter.writeBinaryAttribute(_nextName.getPrefix(), _nextName.getNamespaceURI(), _nextName.getLocalPart(), fullBuffer);
        } else if (checkNextIsUnwrapped()) {
            _xmlWriter.writeBinary(data, offset, len);
        } else {
            if (_xmlPrettyPrinter != null) {
                _xmlPrettyPrinter.writeLeafElement(_xmlWriter, _nextName.getNamespaceURI(), _nextName.getLocalPart(), data, offset, len);
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