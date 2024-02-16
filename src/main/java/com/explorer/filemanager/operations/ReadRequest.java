package com.explorer.filemanager.operations;

import com.explorer.filemanager.data.ErrorDetails;
import com.explorer.filemanager.data.FileManagerDirectoryContent;

public class ReadRequest {
    String action;  // or from enums
    String path;
    Boolean showHiddenItems;
    FileManagerDirectoryContent data; // details about current path (directory)

}
