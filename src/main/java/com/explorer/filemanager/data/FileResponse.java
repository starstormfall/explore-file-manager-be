package com.explorer.filemanager.data;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileResponse {


    @Nullable // used only for READ, SEARCH, null for other operations
    FileContent cwd;

    FileContent[] files;

    @Nullable // used only for DETAILS, null for other operations
    FileContent details;

    ErrorDetails error;


}
