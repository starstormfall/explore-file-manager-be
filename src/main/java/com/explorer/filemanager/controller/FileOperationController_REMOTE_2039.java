package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.pojo.ErrorDetails;
import com.explorer.filemanager.pojo.FileRequestParams;
import com.explorer.filemanager.pojo.FileResponse;
import com.explorer.filemanager.service.FileOperationService;
import com.explorer.filemanager.minio.MongoAndMinioTransactionService;
import com.explorer.filemanager.service.MongoMetadataService;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

enum Action {
	read, create, rename, delete, details, search, copy, move,
}

@Slf4j
@RestController
@RequestMapping(path = "api/v1/workspaces/{workspaceId}/FileOperations")
public class FileOperationController {
	private FileOperationService fileOperationService;
	private MongoMetadataService mongoMetadataService;
	private MongoAndMinioTransactionService transactionService;

	@Autowired
	public FileOperationController(FileOperationService fileOperationService, MongoMetadataService mongoMetadataService,
			MongoAndMinioTransactionService transactionService) {
		this.fileOperationService = fileOperationService;
		this.mongoMetadataService = mongoMetadataService;
		this.transactionService = transactionService;
	}

	@PostMapping
	public FileResponse fileOperation(@PathVariable("workspaceId") String workspaceId,
			@RequestBody FileRequestParams requestParams) throws Exception {

		FileResponse response = new FileResponse();

		/** initialize common values */
		String bucketName = workspaceId;
		Action action = Action.valueOf(requestParams.getAction());
		String path = requestParams.getPath(); // full path from root to cwd
		FileContent[] data = requestParams.getData();
		FileContent targetedLocation = requestParams.getTargetData();

		switch (action) {

		/** READS METADATA FROM MONGO ONLY **/
		case read:
			// request params: String action; String path; Boolean showHiddenItems;
			// FileManagerDirectoryContent data
			// response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[]
			// files; ErrorDetails error;

			/**
			 * for root folder: path is "/" and data is empty, use workspaceId to find all
			 * the child folders
			 **/
			if (path.equals("/") && data.length == 0) {
				response.setCwd(mongoMetadataService.getCwd(workspaceId));
				response.setFiles(mongoMetadataService.getFilesByParentId(workspaceId));
			} else {
				String fileId = data[0].getMongoId();
				response.setCwd(mongoMetadataService.getCwd(fileId));
				response.setFiles(mongoMetadataService.getFilesByParentId(fileId));
			}
			break;

		/**
		 * TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO - to create path in MINIO and
		 * new doc in MONGO atomically
		 **/
		case create:
			// request params: String path; String name; FileManagerDirectoryContent data
			// response: FileManagerDirectoryContent[] files; ErrorDetails error;
			String newFolderName = requestParams.getName();
			String parentId = data[0].getMongoId(); // mongoId of parent folder

			try {
				FileContent newFolderData = mongoMetadataService.createFolder(newFolderName, parentId, path);
				response.setFiles(new FileContent[] { newFolderData });
			} catch (Exception exception) {
				log.error(exception.getLocalizedMessage());
				response.setError(new ErrorDetails("400",
						String.format("A file or folder with the name %s already exists", newFolderName), null));
			}
			break;

		/** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
		// YX
		case rename:
			// request params: String action; String path; String name; String newName;
			// FileManagerDirectoryContent data
			// response: FileManagerDirectoryContent[] files; ErrorDetails error;
			String newName = requestParams.getNewName();

			try {
				FileContent[] renameFiles = mongoMetadataService.renameFile(data, newName);
				response.setFiles(renameFiles);

			} catch (Exception exception) {
				log.error(exception.getMessage());
				response.setError(new ErrorDetails("400", String.format(exception.getMessage()), null));
			}
			break;

		/** TRANSACTION TO DELETE FROM MINIO AND MONGO **/
		case delete:
			// request params: String action; String path; String[] names;
			// FileManagerDirectoryContent data
			// response: FileManagerDirectoryContent[] files; ErrorDetails error;
			System.out.println("Delete");
			try {
				FileContent[] existingFiles = mongoMetadataService.deleteFiles(data);
				response.setFiles(existingFiles);
			} catch (Exception exception) {
				log.error(exception.getLocalizedMessage());
				response.setError(new ErrorDetails("400", "No files were found for deletion", null));
			}
			break;
		/** READS METADATA FROM MONGO ONLY **/
		case details:
			// request params: String action; String path; String[] names;
			// FileManagerDirectoryContent data
			// response: FileManagerDirectoryContent details; ErrorDetails error;

			/** READS METADATA FROM MONGO ONLY **/
		case search:
			// request params: String action; String path; boolean showHiddenItems; boolean
			// caseSensitive; String searchString; FileManagerDirectoryContent data
			// response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[]
			// files; ErrorDetails error;

			/** TRANSACTION TO UPLOAD TO MINIO AND UPDATE MONGO **/
			// YX
		case copy:
			// request params: String action; String path; String[] names; String
			// targetPath; FileManagerDirectoryContent data; String[] renameFiles
			// response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[]
			// files; ErrorDetails error;
			try {
				boolean isRename = requestParams.getRenameFiles().length > 0;
				String targetedPath = requestParams.getTargetPath();
				FileContent[] copyFiles = mongoMetadataService.copyAndMoveFiles(data, targetedLocation, targetedPath,
						isRename, requestParams.getAction());
				response.setFiles(copyFiles);
			} catch (Exception exception) {
				log.error(exception.getLocalizedMessage());
				String[] errorFile = Arrays.stream(data).map(file -> file.getName()).toArray(String[]::new);
				response.setError(new ErrorDetails("400", exception.getMessage(), errorFile));
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
				boolean isRename = requestParams.getRenameFiles().length > 0;
				String targetedPath = requestParams.getTargetPath();
				FileContent[] copyFiles = mongoMetadataService.copyAndMoveFiles(data, targetedLocation, targetedPath,
						isRename, requestParams.getAction());
				response.setFiles(copyFiles);
			} catch (Exception exception) {
				log.error(exception.getLocalizedMessage());
				String[] errorFile = Arrays.stream(data).map(file -> file.getName()).toArray(String[]::new);
				response.setError(new ErrorDetails("400", exception.getMessage(), errorFile));
			}

			break;
			
		}

		return response;

	}

}
