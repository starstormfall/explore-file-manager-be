package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MongoMetadataServiceImpl implements MongoMetadataService {
    private final FileContentRepository repository;
    private final MongoTemplate mongoTemplate;
    @Autowired
    public MongoMetadataServiceImpl(FileContentRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get cwd by id
     * @param fileId mongoId
     * @return metadata of current working directory ELSE null if file does not exist
     */
    @Override
    public FileContent getCwd(String fileId) { return repository.findByMongoId(fileId); }

    /**
     * Get all files with parentId
     * @param parentId mongoId of parent folder
     * @return array of child files' metadata belonging to a parent
     */
    @Override
    public List<FileContent> getFilesByParentId(String parentId) {
        return repository.findByParentId(parentId);
    }


    /**
     * creates a new document (folder entry)
     * updates parent folder to hasChild so that nested folder can show up in frontend tree view
     * @param folderName name of new folder to be created
     * @param parentId mongoId of parent folder to be created in
     * @param path /path/to/parent
     * @return details of new folder
     * @throws Exception (i) if parent folder no longer exists (ii) folder with same name already exists with parentId
     */
	@Transactional(rollbackFor = Exception.class)
	@Override
    public FileContent createFolder(String folderName, String parentId, String path) throws Exception {
		FileContent parentFolder = getParentFolder(parentId, path);
		if (!isFolderDuplicate(folderName, parentId)) {
			ObjectId id = new ObjectId();
			FileContent newFolder = new FileContent(
					id.toString(),
					folderName,
					folderName,
					Instant.now().toString(),
					Instant.now().toString(),
					path,
					false,
					false,
					0,
					"Folder",
					parentId // parentId
			);

			if (!parentFolder.getHasChild()) {
				parentFolder.setHasChild(true);
				parentFolder.setDateModified(String.valueOf(Instant.now()));
				repository.save(parentFolder);
			}

			return repository.save(newFolder);
		} else {
			throw new Exception("Folder/File with same name already exists!");
		}
    }

	/**
	 * Renames selected file
	 * if selected file is a folder, also rename all children's filterPath
	 * @param file
	 * @param newName
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public FileContent renameFile(FileContent file, String newName) throws Exception {
		FileContent parentFolder = getParentFolder(file.getParentId(), file.getFilterPath());
		String oldName = file.getName();
		if (parentFolder != null && !isFolderDuplicate(newName, file.getParentId())) {
			file.setName(newName);
			file.setId(newName);


			file.setNewName(newName);
			file.setDateModified(String.valueOf(Instant.now()));

			// if folder, also rename descendants filterPath
			if (file.getType().equals("Folder")) {
				String descendantFilterPath = file.getFilterPath()+oldName+"/";
				String renamedDescendantFilterPath = file.getFilterPath()+newName+"/";
				List<String> childrenFileIds = getDescendantFileIds(descendantFilterPath);

				log.info(String.format("desc: %s", descendantFilterPath));
				log.info(String.format("rename desc: %s", renamedDescendantFilterPath));

				log.info(String.format(String.valueOf(childrenFileIds)));

				Query query = new Query(Criteria.where("mongoId").in(childrenFileIds));

				Update update = new Update().set("filterPath",
						new Document("$regexReplace",
								new Document("filterPath", "$filterPath")
										.append("regex", sanitizeForRegex(descendantFilterPath))
										.append("replacement", sanitizeForRegex(renamedDescendantFilterPath))
						)
				);


				mongoTemplate.updateMulti(query, update, FileContent.class);
			}
			return repository.save(file);
		} else {
			throw new Exception("Something went wrong...");
		}
	}

	/**
	 * Delete files given a list of filenames
	 * Also deletes the nested folders and files
	 * @param names list of folder names
	 * @param files list of folder details
	 * @return list of remaining files with same parentId after deletion
	 * @throws Exception
	 */
    @Override
    public List<FileContent> deleteFiles(String[] names, List<FileContent> files)  {
        for (FileContent file : files) {
			// get all children folders of selected file and remove them first
			List<String> childrenFolderIds = getDescendantFileIds(file.getFilterPath() + file.getName());
			Query query = new Query(Criteria.where("mongoId").in(childrenFolderIds));
			mongoTemplate.remove(query, FileContent.class);
			// remove selected file
			mongoTemplate.remove(file, "FileContent");
        }

		// all files to be deleted share same parentId and filterPath because only files under the same parent folder can be deleted together
		String parentId = files.get(0).getParentId();
		FileContent parentFolder = repository.findByMongoId(parentId);
        List<FileContent> existingFilesAfterDeletion = repository.findByParentId(parentId);
        // update parentFile to hasChild: false if there is no children folder after removal
        if (existingFilesAfterDeletion.size() == 0) {
            parentFolder.setHasChild(false);
        }
		parentFolder.setDateModified(String.valueOf(Instant.now()));
		repository.save(parentFolder);

        return existingFilesAfterDeletion;
    }


	/**
	 * Searches the current working directory for files/folders with
	 * @param searchString received by frontend by default between *: "*searchString*"
	 * @param filterPath current working directory
	 * @return list of matched files/folders
	 */
    @Override
    public List<FileContent> searchFiles(String searchString, String filterPath) {
		String sanitizedSearchString = searchString.substring(1, searchString.length() -1);
		Query query = new Query();
		if (sanitizedSearchString.equals("*")) {
			query.addCriteria(Criteria.where("filterPath").regex(sanitizeForRegex(filterPath), "i"));
		} else {
			query.addCriteria(
					new Criteria().andOperator(
					Criteria.where("filterPath").is(filterPath),
					Criteria.where("name").regex(sanitizeForRegex(sanitizedSearchString), "i")
			));
		}
		return mongoTemplate.find(query, FileContent.class);
    }

	/**
	 * Create new copied files/folders duplicated from another location
	 * @param names
	 * @param files
	 * @param targetedLocation
	 * @param targetPath
	 * @param isRename
	 * @param action
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<FileContent> copyFiles(String[] names, List<FileContent> files, FileContent targetedLocation, String targetPath, boolean isRename, String action) throws Exception {
		return null;
	}

	/**
	 * Move files/folders when they are CUT and pasted (similar to rename)
	 * @param names
	 * @param files
	 * @param targetedLocation
	 * @param targetPath
	 * @param isRename
	 * @param action
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<FileContent> moveFiles(String[] names, List<FileContent> files, FileContent targetedLocation, String targetPath, boolean isRename, String action) throws Exception {
		return null;
	}

	// ------- HELPER FUNCTIONS ------- //
	/**
	 * Checks if there is an existing folder of same name in the parent folder
	 * @param folderName folder name
	 * @param parentId mongoId of parent folder
	 * @return TRUE if there is an existing folder of same name | FALSE if not
	 * @throws Exception "Folder/File with same name exists"
	 */
	private boolean isFolderDuplicate(String folderName, String parentId) {
		Query query = new Query(Criteria.where("parentId").is(parentId).and("name").is(folderName));
		if (mongoTemplate.findOne(query, FileContent.class) != null){
			return true;
		} else {
			return false;
		}
	};

	/**
	 * Checks if parent folder still exists before performing operations
	 * @param parentId  mongoId of parent folder
	 * @param path /path/to/parent
	 * @return parentFolder
	 * @throws Exception "/path/to/parent does not exist."
	 */
	private FileContent getParentFolder(String parentId, String path) throws Exception {
		FileContent parentFolder = repository.findById(new ObjectId(parentId)).orElse(null);
		if (parentFolder != null) {
			return parentFolder;
		} else {
			throw new Exception(String.format("%s does not exist.", path));
		}
	};

	/**
	 * Gets mongoIds of all children and children's folder with same parent path (filterPath)
	 * @param filterPath relative path of the child folder
	 * @return list of children folders' mongoIds
	 */
	private List<String> getDescendantFileIds(String filterPath) {
		log.info(String.format("filterPath: %s", filterPath));
		Query query = new Query(Criteria.where("filterPath").regex(sanitizeForRegex(filterPath)));
		List<FileContent> childrenFolders = mongoTemplate.find(query, FileContent.class);
		return childrenFolders.stream()
				.map(FileContent::getMongoId)
				.collect(Collectors.toList());
	}


	/**
	 * Adds backslash to escape special characters
	 * @param string relative path of the child folder
	 * @return sanitized string suitable for regex query
	 */
	private String sanitizeForRegex(String string) {
		String specialCharacters = "[()*],";
		String sanitizedString = string.replaceAll(specialCharacters, "\\\\$0");
		log.info(String.format("string: %s", string));
		log.info(String.format("sanitized: %s", sanitizedString));
		return sanitizedString;
	}



	//	public void updateChildPath(FileContent childFile, String oldFileName, String newFileName) {
//
//		if (childFile != null) {
//			String oldPath = childFile.getFilterPath();
//			String[] paths = oldPath.split("/");
//
//			for (int i = paths.length - 1; i >= 0; i--) {
//
//				if (paths[i].equals(oldFileName)) {
//					paths[i] = newFileName;
//					String newPath = String.join("/", paths) + "/";
//					childFile.setFilterPath(newPath);
//				}
//			}
//			log.info("Saving Child" + childFile.getName());
//			repository.save(childFile);
//		}
//		if (childFile.getHasChild()) {
//			FileContent[] childFiles = repository.findByParentId(childFile.getMongoId());
//			for (FileContent child : childFiles) {
//				updateChildPath(child, oldFileName, newFileName);
//			}
//		}
//
//	}
//
//
//	public void createChildFolder(FileContent file, String newFileName, String newParentId) {
//		if (file != null) {
//			/*
//			 * (@NonNull String mongoId, @NonNull String id, @NonNull String name, @NonNull
//			 * String dateCreated, @NonNull String dateModified, @NonNull String
//			 * filterPath, @NonNull Boolean hasChild, @NonNull Boolean isFile, @NonNull
//			 * Number size, @NonNull String type, @NonNull String parentId)
//			 */
//			ObjectId newId = new ObjectId();
//			FileContent newData = new FileContent(newId.toString(), file.getName(), file.getName(),
//					Instant.now().toString(), Instant.now().toString(), newFileName, file.getHasChild(),
//					file.getIsFile(), file.getSize(), file.getType(), newParentId);
//
//			repository.save(newData);
//
//			if (file.getHasChild()) {
//				FileContent[] childFiles = repository.findByParentId(file.getMongoId());
//
//				for (FileContent child : childFiles) {
//					String newChildPath = newFileName + child.getName() + "/";
//					createChildFolder(child, newChildPath, newId.toString());
//				}
//			}
//		}
//
//	}
//
//	@Override
//	public List<FileContent> renameFile(FileContent[] files, String newName) throws Exception {

//		try {
//			String fileId = files[0].getMongoId();
//
//			FileContent selectedFile = repository.findById(new ObjectId(fileId)).get();
//			if (selectedFile.getFilterPath().length() > 0) {
//				FileContent[] existingFiles = repository.findByFilterPath(selectedFile.getFilterPath());
//				boolean isExist = false;
//				if (existingFiles.length > 0) {
//					for (FileContent existingFile : existingFiles) {
//						if (existingFile.getName().equals(newName)) {
//							isExist = true;
//							break;
//						}
//					}
//					if (isExist) {
//						throw new Exception("Folder/Files with same name exists");
//					} else {
//						String oldName = selectedFile.getName();
//						// update the fields
//						selectedFile.setId(newName);
//						selectedFile.setDateModified(files[0].getDateModified());
//						selectedFile.setName(newName);
//						updateChildPath(selectedFile, oldName, newName);
//					}
//				}
//			}
//			// refetch the data
//
//			FileContent[] updatedFiles = Arrays.stream(files).flatMap(file -> {
//				FileContent[] existingFilesAfterUpdate = repository.findByParentId(file.getMongoId());
//				return Stream.of(existingFilesAfterUpdate);
//			}).toArray(FileContent[]::new);
//
//			return updatedFiles;
//		} catch (Exception Error) {
//			throw Error;
//		}
//	}
//
//
//
//	@Override
//	public List<FileContent> copyAndMoveFiles(FileContent[] files, FileContent targetedLocation, String targetPath,
//			boolean isRename, String action) throws Exception {
//
//		try {
//			Action actionType = Action.valueOf(action);
//			// Get Targeted location files
//			FileContent[] targetedPathFiles = repository.findByFilterPath(targetPath);
//
//			for (FileContent file : files) {
//				boolean isExist = false;
//				if (targetedPathFiles.length > 0) {
//
//					if (!isRename)
//						for (FileContent targetedPathFile : targetedPathFiles) {
//							if (targetedPathFile.getName().equals(file.getName())) {
//								isExist = true;
//								throw new Exception("File Already Exists");
//							}
//						}
//					if (!isExist) {
//						if (isRename) {
//
//							ObjectId newId = new ObjectId();
//							String renameName;
//							int counter = 1;
//							String newParentId = targetedPathFiles[0].getParentId();// get the parent id
//							while (true) {
//								renameName = file.getName() + "(" + counter + ")";
//								boolean nameExist = Arrays.stream(targetedPathFiles)
//										.map(targetFile -> targetFile.getName()).anyMatch(renameName::equals);
//								if (!nameExist) {
//									break;
//								}
//								counter++;
//							}
//
//							FileContent newFolder = new FileContent(newId.toString(), renameName, renameName,
//									Instant.now().toString(), Instant.now().toString(), targetPath, file.getHasChild(),
//									file.getIsFile(), file.getSize(), file.getType(), newParentId);
//							repository.save(newFolder);
//
//							// if selected file has child
//							FileContent[] childFiles = repository.findByParentId(file.getMongoId());
//							for (FileContent child : childFiles) {
//								String newPath = targetPath + renameName + "/";
//								createChildFolder(child, newPath, newId.toString());
//
//							}
//							// Update the parent folder hasChild status
//							targetedLocation.setHasChild(true);
//							repository.save(targetedLocation);
//
//						} else {
//							ObjectId newId = new ObjectId();
//
//							String newParentId = targetedPathFiles[0].getParentId();// get the parent id
//
//							FileContent newFolder = new FileContent(newId.toString(), file.getName(), file.getName(),
//									Instant.now().toString(), Instant.now().toString(), targetPath, file.getHasChild(),
//									file.getIsFile(), file.getSize(), file.getType(), newParentId);
//							repository.save(newFolder);
//
//							// if selected file has child
//							FileContent[] childFiles = repository.findByParentId(file.getMongoId());
//							for (FileContent child : childFiles) {
//								String newPath = targetPath + file.getName() + "/";
//								createChildFolder(child, newPath, newId.toString());
//
//							}
//							// Update the parent folder hasChild status
//							targetedLocation.setHasChild(true);
//							repository.save(targetedLocation);
//						}
//					}
//				} else {
//					ObjectId newId = new ObjectId();
//
//					String newParentId = targetedLocation.getMongoId();// get the parent id
//
//					FileContent newFolder = new FileContent(newId.toString(), file.getName(), file.getName(),
//							Instant.now().toString(), Instant.now().toString(), targetPath, file.getHasChild(),
//							file.getIsFile(), file.getSize(), file.getType(), newParentId);
//					repository.save(newFolder);
//
//					// if selected file has child
//					FileContent[] childFiles = repository.findByParentId(file.getMongoId());
//					for (FileContent child : childFiles) {
//						String newPath = targetPath + file.getName() + "/";
//						createChildFolder(child, newPath, newId.toString());
//
//					}
//					// Update the parent folder hasChild status
//					targetedLocation.setHasChild(true);
//					repository.save(targetedLocation);
//				}
//			}
//
//
//// FROM HB: `copy` action has names[] in request params as well, can use it for deletion
////			if (actionType.equals(Action.move)) {
////				deleteFiles(files);
////			}
//
//			FileContent[] updatedFiles = Arrays.stream(files).flatMap(file -> {
//				FileContent[] existingFilesAfterUpdate = repository.findByParentId(file.getMongoId());
//				return Stream.of(existingFilesAfterUpdate);
//			}).toArray(FileContent[]::new);
//
//			return updatedFiles;
//
//		} catch (Exception error) {
//			throw error;
//		}
//	}

}
