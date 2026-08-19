// ===== FIXED com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer :: _verifySharing() [lines 874-887] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-11-fixed/src/main/java/com/fasterxml/jackson/core/sym/ByteQuadsCanonicalizer.java =====
    private void _verifySharing()
    {
        if (_hashShared) {
            _hashArea = Arrays.copyOf(_hashArea, _hashArea.length);
            _names = Arrays.copyOf(_names, _names.length);
            _hashShared = false;
            // 09-Sep-2015, tatu: As per [jackson-core#216], also need to ensure
            //    we rehash as needed, as need-rehash flag is not copied from parent
            _verifyNeedForRehash();
        }
        if (_needRehash) {
            rehash();
        }
    }
