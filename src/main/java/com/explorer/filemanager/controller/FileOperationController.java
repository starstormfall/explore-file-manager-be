package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.pojo.ErrorDetails;
import com.explorer.filemanager.pojo.FileDetails;
import com.explorer.filemanager.pojo.FileRequestParams;
import com.explorer.filemanager.pojo.FileResponse;
import com.explorer.filemanager.service.FileOperationService;
import com.explorer.filemanager.minio.MongoAndMinioTransactionService;
import com.explorer.filemanager.service.MongoMetadataService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        String path = requestParams.getPath(); // full path from root to cwd
        List<FileContent> data = requestParams.getData();
        FileContent targetedLocation = requestParams.getTargetData();


        switch(action) {

            /** READS METADATA FROM MONGO ONLY **/
            case read:
                try {
                    /** for root folder: path is "/" and data is empty, use workspaceId to find all the child folders  **/
                    if (path.equals("/") && data.size() == 0) {
                        response.setCwd(mongoMetadataService.getCwd(workspaceId));
                        response.setFiles(mongoMetadataService.getFilesByParentId(workspaceId));
                    } else {
                        String fileId = data.get(0).getMongoId();
                        response.setCwd(mongoMetadataService.getCwd(fileId));
                        response.setFiles(mongoMetadataService.getFilesByParentId(fileId));
                    }
                }   catch (Exception exception){
                    log.error(exception.getLocalizedMessage());
                    response.setError(new ErrorDetails(
                            "404",
                            String.format("A file or folder with the name %s may have recently been deleted or moved. Please refresh your browser.", data.get(0).getName()),
                            null
                    ));
                }
                break;

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO - to create path in MINIO and new doc in MONGO atomically **/
            case create:
                String newFolderName = requestParams.getName();
                String parentId = data.get(0).getMongoId(); // mongoId of parent folder
                try {
                    FileContent newFolderData = mongoMetadataService.createFolder(newFolderName, parentId, path);
                    response.setFiles(new ArrayList<>(List.of(newFolderData)));
                } catch (Exception exception){
                    if (exception.getMessage() == "No value present") {
                        response.setError(new ErrorDetails(
                                "404",
                                String.format("Folder %s no longer exists. Please refresh your browser.", path),
                                null
                        ));
                    } else {
                        response.setError(new ErrorDetails(
                                "400",
                                "Folder with same name exists.",
                                null
                        ));
                    }
                }
                break;

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            // YX
            case rename:
			// request params: String action; String path; String name; String newName; FileManagerDirectoryContent data
			// response: FileManagerDirectoryContent[] files; ErrorDetails error;
                String newName = requestParams.getNewName();
                try {
                    FileContent renamedFile = mongoMetadataService.renameFile(data.get(0), newName);
                    response.setFiles(new ArrayList<>(List.of(renamedFile)));
                } catch (Exception exception) {
                    log.error(exception.getMessage());
                    response.setError(new ErrorDetails("400", String.format(exception.getMessage()), null));
                }
                break;

            /** TRANSACTION TO DELETE FROM MINIO AND MONGO **/
            case delete:
                try {
                    String[] fileNames = requestParams.getNames();
                    mongoMetadataService.deleteFiles(fileNames, data);
                    response.setFiles(data);
                } catch (Exception exception){
                    response.setError(new ErrorDetails(
                            "400",
                            "No files were found for deletion",
                            null
                    ));
                }


            /** READS METADATA FROM MONGO ONLY **/
            case details:
                FileDetails details = new FileDetails();
                // for multiple files selected
                if (requestParams.getNames().length > 1) {
                    String[] fileNamesList = requestParams.getNames();
                    String fileNames = "";
                    for (int i = 0 ; i <fileNamesList.length; i++) {
                        if (i > 0) {
                            fileNames += ", ";
                        }
                        fileNames += fileNamesList[i];
                    }
                    details.setMultipleFiles(true);
                    details.setName(fileNames);
                    details.setIsFile(false);
                    details.setLocation(data.get(0).getFilterPath());
                } else{
                    details.setMultipleFiles(false);
                    details.setLocation(data.get(0).getFilterPath() + data.get(0).getName() + "/");
                    details.setName(data.get(0).getName());
                    details.setSize(data.get(0).getSize().toString());
                    details.setCreated(data.get(0).getDateCreated());
                    details.setModified(data.get(0).getDateModified());
                    details.setIsFile(data.get(0).getIsFile());
                }
                response.setDetails(details);

            /** READS METADATA FROM MONGO ONLY **/
            case search:
                FileContent topFolder = new FileContent();
                List<FileContent> foundFiles = mongoMetadataService.searchFiles(requestParams.getSearchString(), path);
                response.setCwd(topFolder);
                response.setFiles(foundFiles);

            /** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
            // YX
            case copy:
			// request params: String action; String path; String[] names; String
			// targetPath; FileManagerDirectoryContent data; String[] renameFiles
			// response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[]
			// files; ErrorDetails error;
			try {
//				boolean isRename = requestParams.getRenameFiles().length > 0;
//				String targetedPath = requestParams.getTargetPath();
//				FileContent[] copyFiles = mongoMetadataService.copyAndMoveFiles(data, targetedLocation, targetedPath,
//						isRename, requestParams.getAction());
//				response.setFiles(copyFiles);
			} catch (Exception exception) {
//				log.error(exception.getLocalizedMessage());
//				String[] errorFile = Arrays.stream(data).map(file -> file.getName()).toArray(String[]::new);
//				response.setError(new ErrorDetails("400", exception.getMessage(), errorFile));
			}

			break;
		/** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
		// YX
		case move:
			// request params: String action; String path; String[] names; String
			// targetPath; FileManagerDirectoryContent data; String[] renameFiles
			// response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[]
			// files; ErrorDetails error;
			System.out.println("Moving");
			try {
//				boolean isRename = requestParams.getRenameFiles().length > 0;
//				String targetedPath = requestParams.getTargetPath();
//				FileContent[] copyFiles = mongoMetadataService.copyAndMoveFiles(data, targetedLocation, targetedPath,
//						isRename, requestParams.getAction());
//				response.setFiles(copyFiles);
			} catch (Exception exception) {
//				log.error(exception.getLocalizedMessage());
//				String[] errorFile = Arrays.stream(data).map(file -> file.getName()).toArray(String[]::new);
//				response.setError(new ErrorDetails("400", exception.getMessage(), errorFile));
			}

			break;
			
		}
        return response;





    }





}
