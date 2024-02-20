package com.explorer.filemanager.controller;

import com.explorer.filemanager.data.FileContent;
import com.explorer.filemanager.data.FileRequestParams;
import com.explorer.filemanager.data.FileResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

enum Action {
    READ,
    CREATE,
    RENAME,
    DELETE,
    DETAILS,
    SEARCH,
    COPY,
    MOVE,
}
@RestController
@RequestMapping(path="api/v1/FileOperations")
public class FileOperationController {

    // READ
    @PutMapping
    public FileResponse fileOperation(@RequestBody FileRequestParams requestParams) {

        Action action = Action.valueOf(requestParams.getAction());
        String path = requestParams.getPath();
        FileContent data = requestParams.getData();

        FileResponse response = new FileResponse();

        switch(action) {

            case READ:
                // request params: String action; String path; Boolean showHiddenItems; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;



                break;

            case CREATE:
                // request params: String action; String path; String name; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;

                break;
            case RENAME:
                // request params: String action; String path; String name; String newName; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;

            case DELETE:
                // request params: String action; String path; String[] names; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;

            case DETAILS:
                // request params: String action; String path; String[] names; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent details; ErrorDetails error;

            case SEARCH:
                // request params: String action; String path; boolean showHiddenItems; boolean caseSensitive; String searchString; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

            case COPY:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

            case MOVE:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;


        }

        return response;





    }





}
