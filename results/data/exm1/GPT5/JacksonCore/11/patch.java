private void _verifySharing()
    {
        if (_hashShared) {
            _hashArea = Arrays.copyOf(_hashArea, _hashArea.length);
            _names = Arrays.copyOf(_names, _names.length);
            _hashShared = false;
            // Parent may have required rehash; ensure we rehash after unsharing
            rehash();
            return;
        }
        if (_needRehash) {
            rehash();
        }
    }