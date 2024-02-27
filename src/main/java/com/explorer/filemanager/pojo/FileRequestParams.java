package com.explorer.filemanager.pojo;

import com.explorer.filemanager.model.FileContent;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileRequestParams {

    String action;

    String path;

    FileContent[] data;

    @Nullable
    Boolean showHiddenItems;


    String newName;

    String[] names;



}
