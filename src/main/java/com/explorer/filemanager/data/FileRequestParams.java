package com.explorer.filemanager.data;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileRequestParams {

    String action;

    String path;

    FileContent data;

    @Nullable
    Boolean showHiddenItems;


    String newName;

    String[] names;



}
