package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.pojo.FileRequestParams;
import com.explorer.filemanager.pojo.FileResponse;
import com.explorer.filemanager.service.FileOperationService;
import com.explorer.filemanager.minio.MongoAndMinioTransactionService;
import com.explorer.filemanager.service.MongoMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@Slf4j
@RestController
@RequestMapping(path="api/v1/workspaces/{workspaceId}/FileOperations")
public class FileOperationController {
    private FileOperationService fileOperationService;
    private MongoMetadataService mongoMetadataService;
    private MongoAndMinioTransactionService transactionService;


    @Autowired
    public FileOperationController(
            FileOperationService fileOperationService,
            MongoMetadataService mongoMetadataService,
            MongoAndMinioTransactionService transactionService
    ) {
        this.fileOperationService = fileOperationService;
        this.mongoMetadataService = mongoMetadataService;
        this.transactionService = transactionService;

    }

    @PostMapping
    public FileResponse fileOperation(@PathVariable("workspaceId") String workspaceId, @RequestBody FileRequestParams requestParams) throws Exception {

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
                    String fileId = data[0].getMongoId();
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

                String parentId = data[0].getId();
                String newFolderName = requestParams.getName();
                transactionService.createFolderTransaction(workspaceId, newFolderName, parentId, path);
                break;

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            // YX
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
            // YX
            case copy:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            // YX
            case move:
                // request params: String action; String path; String[] names; String targetPath; FileManagerDirectoryContent data; String[] renameFiles
                // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;


        }

        return response;





    }





}
