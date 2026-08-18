public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
{
    // Deserialize the string value from JSON
    String value = p.getValueAsString();
    // Prefix is stored as _prefix field in enclosing class
    return new MyBean(_prefix + value);
}