package com.explorer.filemanager.pojo;

import com.explorer.filemanager.model.FileContent;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FileRequestParams {
    String action;
    String path;  // full path to current cwd
    List<FileContent> data;
    String[] renameFiles;
    FileContent targetData;
    String targetPath;

    @Nullable Boolean showHiddenItems;
    @Nullable String newName;
    @Nullable String[] names;
    @Nullable String name; // used for action 'create'
    @Nullable String searchString;
}
