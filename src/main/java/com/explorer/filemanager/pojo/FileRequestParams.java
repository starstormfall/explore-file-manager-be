package com.explorer.filemanager.pojo;

import com.explorer.filemanager.model.FileContent;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileRequestParams {
    String action;
    String path;  // full path to current cwd
    FileContent[] data;
    @Nullable Boolean showHiddenItems;
    @Nullable String newName;
    @Nullable String[] names;
    @Nullable String name; // used for action 'create'
    @Nullable String searchString;
}
