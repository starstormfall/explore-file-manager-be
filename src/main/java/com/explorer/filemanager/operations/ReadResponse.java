package com.explorer.filemanager.operations;

import com.explorer.filemanager.data.ErrorDetails;
import com.explorer.filemanager.data.FileManagerDirectoryContent;

public class ReadResponse {
    FileManagerDirectoryContent cwd; // path (current working dir) details
    FileManagerDirectoryContent[] files; // details of files and folders present in given path or dir
    ErrorDetails error; // error details
}
