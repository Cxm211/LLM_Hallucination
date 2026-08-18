private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    out.writeInt(size());
    for (int i = 0; i < size(); i++) {
        out.writeObject(buffer[(head + i) % buffer.length]);
    }
}