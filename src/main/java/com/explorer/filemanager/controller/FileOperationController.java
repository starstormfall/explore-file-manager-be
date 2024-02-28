package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.pojo.FileRequestParams;
import com.explorer.filemanager.pojo.FileResponse;
import com.explorer.filemanager.service.FileOperationService;
import com.explorer.filemanager.service.MongoMetadataService;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
@RequestMapping(path="api/v1/workspaces/{workspaceId}/FileOperations")
public class FileOperationController {

    private FileOperationService fileOperationService;

    private MongoMetadataService mongoMetadataService;


    @Autowired
    public FileOperationController(
            FileOperationService fileOperationService,
            MongoMetadataService mongoMetadataService
    ) {
        this.fileOperationService = fileOperationService;
        this.mongoMetadataService = mongoMetadataService;
    }

    @PostMapping
    public FileResponse fileOperation(@PathVariable("workspaceId") String workspaceId, @RequestBody FileRequestParams requestParams) {

        FileResponse response = new FileResponse();

        /** initialize common values */
        String bucketName = workspaceId;
        Action action = Action.valueOf(requestParams.getAction());
        String path = requestParams.getPath(); // full path from root e.g. /first-folder-id/ | /first-folder-id/nested-folder-id/
        FileContent[] data = requestParams.getData();

        switch(action) {

            /** READS METADATA FROM MONGO ONLY **/
            case read:
                // request params: String action; String path; Boolean showHiddenItems; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

                /** for root folder: path is "/" and data is empty  **/
                if (path.equals("/") && data.length == 0 ) {
                    response.setCwd(mongoMetadataService.getCwd(workspaceId));
                    response.setFiles(mongoMetadataService.getFilesByParentId(workspaceId));
                } else {
                    String fileId = data[0].getId();
                    response.setCwd(mongoMetadataService.getCwd(fileId));
                    response.setFiles(mongoMetadataService.getFilesByParentId(fileId));
                }

                break;

            /** creates folder only, not the same as UPLOAD */
            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            // updates MINIO first then create document/metadata in MONGO
            case create:
                // request params: String path; String name; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;
                fileOperationService.createFolder(bucketName, path);
                break;

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            case rename:
                // request params: String action; String path; String name; String newName; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;

            /** TRANSACTION TO DELETE FROM MINIO AND MONGO **/
            case delete:
                // request params: String action; String path; String[] names; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent[] files; ErrorDetails error;


            /** READS METADATA FROM MONGO ONLY **/
            case details:
                // request params: String action; String path; String[] names; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent details; ErrorDetails error;

            /** READS METADATA FROM MONGO ONLY **/
            case search:
                // request params: String action; String path; boolean showHiddenItems; boolean caseSensitive; String searchString; FileManagerDirectoryContent data
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            case copy:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            case move:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;


        }

        return response;





    }





}
