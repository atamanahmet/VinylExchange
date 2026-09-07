package com.atamanahmet.vinylexchange.domain.enums;

public enum StorageProvider {
    CLOUDINARY,
    LOCAL,

    /**
     * Image hosted externally and not owned by our storage system (e.g. hotlinked Cover Art Archive
     * placeholder). Never uploaded or deleted by our storage services — only referenced by URL.
     */
    EXTERNAL
}