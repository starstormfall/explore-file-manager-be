package com.explorer.filemanager.pojo;

import com.explorer.filemanager.model.FileContent;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileResponse {


    @Nullable // used only for READ, SEARCH, null for other operations
    FileContent cwd;

    FileContent[] files;

    @Nullable // used only for DETAILS, null for other operations
    FileContent details;

    ErrorDetails error;


}
