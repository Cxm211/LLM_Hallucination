private void _verifySharing()
{
    if (_hashShared) {
        _hashArea = Arrays.copyOf(_hashArea, _hashArea.length);
        _names = Arrays.copyOf(_names, _names.length);
        _hashShared = false;
        // 09-Sep-2015, tatu: As per [jackson-core#216], also need to ensure
        //    we rehash as needed, as need-rehash flag is not copied from parent
        // Need to check rehash status AFTER copying since parent's _needRehash is not copied
        if (_needRehash) {
            rehash();
        }
    } else if (_needRehash) {
        rehash();
    }
}