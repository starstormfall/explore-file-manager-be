package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.dto.FileRequestParams;
import com.explorer.filemanager.dto.FileResponse;
import com.explorer.filemanager.repository.FileContentRepository;
import com.explorer.filemanager.service.FileOperationService;
import com.github.javafaker.Faker;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

enum Action {
    read,
    create,
    rename,
    delete,
    details,
    search,
    copy,
    move,
}



@RestController
@RequestMapping(path="api/workspaces/{workspaceId}/v1/FileOperations")
public class FileOperationController {

    private FileOperationService fileOperationService;


    @Autowired
    public FileOperationController(FileOperationService fileOperationService) {
        this.fileOperationService = fileOperationService;
    }

    @PutMapping
    public FileResponse fileOperation(@PathVariable("workspaceId") String workspaceId, @RequestBody FileRequestParams requestParams) {

        FileResponse response = new FileResponse();

        /** initialize common values */
        String bucketName = workspaceId;
        Action action = Action.valueOf(requestParams.getAction());
        String path = requestParams.getPath(); // full path from root e.g. /first-level-folder/ | /first-level/second-level/
        FileContent data = requestParams.getData();





        switch(action) {

            // reads from mongo
            case read:
                // request params: String action; String path; Boolean showHiddenItems; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;


                break;

            /** creates folder only, not the same as UPLOAD */
            // updates MINIO first then create document/metadata in MONGO
            case create:
                // request params: String path; String name; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;
                fileOperationService.createFolder(bucketName, path);

                break;

            // updates MINIO first then create document/metadata in MONGO
            case rename:
                // request params: String action; String path; String name; String newName; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;

            // updates MINIO first then create document/metadata in MONGO
            case delete:
                // request params: String action; String path; String[] names; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;

            // updates MINIO first then create document/metadata in MONGO
            case details:
                // request params: String action; String path; String[] names; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent details; ErrorDetails error;

            // updates MINIO first then create document/metadata in MONGO
            case search:
                // request params: String action; String path; boolean showHiddenItems; boolean caseSensitive; String searchString; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

            // updates MINIO first then create document/metadata in MONGO
            case copy:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

            // updates MINIO first then create document/metadata in MONGO
            case move:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;


        }

        return response;





    }





}
