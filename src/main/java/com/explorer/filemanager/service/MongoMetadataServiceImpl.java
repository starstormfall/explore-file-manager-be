package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
    public FileContent getCwd(String fileId) {
        return repository.findById(new ObjectId(fileId)).orElse(null);
    }

    /**
     * Get all files with parentId
     * @param parentId mongoId of parent folder
     * @return array of child files' metadata belonging to a parent
     */
    @Override
    public FileContent[] getFilesByParentId(String parentId) {
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
    @Override
    public FileContent createFolder(String folderName, String parentId, String path) throws Exception {

		FileContent parentFolder = isParentFolderPresent(parentId, path);
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
					"",
					parentId // parentId
			);

			if (!parentFolder.getHasChild()) {
				parentFolder.setHasChild(true);
				repository.save(parentFolder);
			}

			return repository.save(newFolder);
		} else {
			throw new Exception("Something went wrong");
		}
    }

    /**
     * Delete files given a list of filenames
     * Also deletes the nested folders and files
     * @param files
     * @return
     * @throws Exception
     */
	/**
	 * Delete files given a list of filenames
	 * Also deletes the nested folders and files
	 * @param names list of folder names
	 * @param files list of folder details
	 * @return list of remaining files with same parentId after deletion
	 * @throws Exception
	 */
    @Override
    public FileContent[] deleteFiles(String[] names, FileContent[] files)  {

        // all files will share same parentId because only files under the same parent folder can be deleted together
        String parentId = files[0].getParentId();
        String filterPath = files[0].getFilterPath();

        List<String> fileIds = new ArrayList<>();
        List<String> parentFilterPaths = new ArrayList<>();

        for (FileContent file : files) {

            fileIds.add(file.getMongoId());
            parentFilterPaths.add(filterPath+file.getName()+"/");
        }

        Criteria criteria = new Criteria().orOperator(
            // find selected files by filename AND parentId
            Criteria.where("name").in(names).and("parentId").is(parentId),
            // get child of selected files with child's parentId that matches the mongoId of the files to be deleted
            Criteria.where("filterPath").in(parentFilterPaths)
        );

        Query query = new Query(criteria);

        mongoTemplate.findAllAndRemove(query, FileContent.class);

        FileContent[] existingFilesAfterDeletion = repository.findByParentId(parentId);

        // update parentFile to hasChild: false if there are no more child files after removal
        if (existingFilesAfterDeletion.length == 0) {
            FileContent parentFolder = repository.findById(new ObjectId(parentId)).get();
            parentFolder.setHasChild(false);
            repository.save(parentFolder);
        }

        return existingFilesAfterDeletion;

    }

    @Override
    public FileContent[] searchFiles(String searchString, String path) {
        return new FileContent[0];
    }

	public void updateChildPath(FileContent childFile, String oldFileName, String newFileName) {

		if (childFile != null) {
			String oldPath = childFile.getFilterPath();
			String[] paths = oldPath.split("/");

			for (int i = paths.length - 1; i >= 0; i--) {

				if (paths[i].equals(oldFileName)) {
					paths[i] = newFileName;
					String newPath = String.join("/", paths) + "/";
					childFile.setFilterPath(newPath);
				}
			}
			log.info("Saving Child" + childFile.getName());
			repository.save(childFile);
		}
		if (childFile.getHasChild()) {
			FileContent[] childFiles = repository.findByParentId(childFile.getMongoId());
			for (FileContent child : childFiles) {
				updateChildPath(child, oldFileName, newFileName);
			}
		}

	}


	public void createChildFolder(FileContent file, String newFileName, String newParentId) {
		if (file != null) {
			/*
			 * (@NonNull String mongoId, @NonNull String id, @NonNull String name, @NonNull
			 * String dateCreated, @NonNull String dateModified, @NonNull String
			 * filterPath, @NonNull Boolean hasChild, @NonNull Boolean isFile, @NonNull
			 * Number size, @NonNull String type, @NonNull String parentId)
			 */
			ObjectId newId = new ObjectId();
			FileContent newData = new FileContent(newId.toString(), file.getName(), file.getName(),
					Instant.now().toString(), Instant.now().toString(), newFileName, file.getHasChild(),
					file.getIsFile(), file.getSize(), file.getType(), newParentId);

			repository.save(newData);

			if (file.getHasChild()) {
				FileContent[] childFiles = repository.findByParentId(file.getMongoId());

				for (FileContent child : childFiles) {
					String newChildPath = newFileName + child.getName() + "/";
					createChildFolder(child, newChildPath, newId.toString());
				}
			}
		}

	}

	@Override
	public FileContent[] renameFile(FileContent[] files, String newName) throws Exception {
		/*
		 * Step 1: Find all the related Folders/ files that belong to the same path and
		 * extract the details Step 2: Check if the name exist, if it exist, throw error
		 * Step 3:Else replace the name and directory fo the path. Step 4:Check if it
		 * has a child. if have retrieve all the child and update the paths.
		 */
		try {
			String fileId = files[0].getMongoId();

			FileContent selectedFile = repository.findById(new ObjectId(fileId)).get();
			if (selectedFile.getFilterPath().length() > 0) {
				FileContent[] existingFiles = repository.findByFilterPath(selectedFile.getFilterPath());
				boolean isExist = false;
				if (existingFiles.length > 0) {
					for (FileContent existingFile : existingFiles) {
						if (existingFile.getName().equals(newName)) {
							isExist = true;
							break;
						}
					}
					if (isExist) {
						throw new Exception("Folder/Files with same name exists");
					} else {
						String oldName = selectedFile.getName();
						// update the fields
						selectedFile.setId(newName);
						selectedFile.setDateModified(files[0].getDateModified());
						selectedFile.setName(newName);
						updateChildPath(selectedFile, oldName, newName);
					}
				}
			}
			// refetch the data

			FileContent[] updatedFiles = Arrays.stream(files).flatMap(file -> {
				FileContent[] existingFilesAfterUpdate = repository.findByParentId(file.getMongoId());
				return Stream.of(existingFilesAfterUpdate);
			}).toArray(FileContent[]::new);

			return updatedFiles;
		} catch (Exception Error) {
			throw Error;
		}
	}



	@Override
	public FileContent[] copyAndMoveFiles(FileContent[] files, FileContent targetedLocation, String targetPath,
			boolean isRename, String action) throws Exception {

		try {
			Action actionType = Action.valueOf(action);
			// Get Targeted location files
			FileContent[] targetedPathFiles = repository.findByFilterPath(targetPath);

			for (FileContent file : files) {
				boolean isExist = false;
				if (targetedPathFiles.length > 0) {

					if (!isRename)
						for (FileContent targetedPathFile : targetedPathFiles) {
							if (targetedPathFile.getName().equals(file.getName())) {
								isExist = true;
								throw new Exception("File Already Exists");
							}
						}
					if (!isExist) {
						if (isRename) {

							ObjectId newId = new ObjectId();
							String renameName;
							int counter = 1;
							String newParentId = targetedPathFiles[0].getParentId();// get the parent id
							while (true) {
								renameName = file.getName() + "(" + counter + ")";
								boolean nameExist = Arrays.stream(targetedPathFiles)
										.map(targetFile -> targetFile.getName()).anyMatch(renameName::equals);
								if (!nameExist) {
									break;
								}
								counter++;
							}

							FileContent newFolder = new FileContent(newId.toString(), renameName, renameName,
									Instant.now().toString(), Instant.now().toString(), targetPath, file.getHasChild(),
									file.getIsFile(), file.getSize(), file.getType(), newParentId);
							repository.save(newFolder);

							// if selected file has child
							FileContent[] childFiles = repository.findByParentId(file.getMongoId());
							for (FileContent child : childFiles) {
								String newPath = targetPath + renameName + "/";
								createChildFolder(child, newPath, newId.toString());

							}
							// Update the parent folder hasChild status
							targetedLocation.setHasChild(true);
							repository.save(targetedLocation);

						} else {
							ObjectId newId = new ObjectId();

							String newParentId = targetedPathFiles[0].getParentId();// get the parent id

							FileContent newFolder = new FileContent(newId.toString(), file.getName(), file.getName(),
									Instant.now().toString(), Instant.now().toString(), targetPath, file.getHasChild(),
									file.getIsFile(), file.getSize(), file.getType(), newParentId);
							repository.save(newFolder);

							// if selected file has child
							FileContent[] childFiles = repository.findByParentId(file.getMongoId());
							for (FileContent child : childFiles) {
								String newPath = targetPath + file.getName() + "/";
								createChildFolder(child, newPath, newId.toString());

							}
							// Update the parent folder hasChild status
							targetedLocation.setHasChild(true);
							repository.save(targetedLocation);
						}
					}
				} else {
					ObjectId newId = new ObjectId();

					String newParentId = targetedLocation.getMongoId();// get the parent id

					FileContent newFolder = new FileContent(newId.toString(), file.getName(), file.getName(),
							Instant.now().toString(), Instant.now().toString(), targetPath, file.getHasChild(),
							file.getIsFile(), file.getSize(), file.getType(), newParentId);
					repository.save(newFolder);

					// if selected file has child
					FileContent[] childFiles = repository.findByParentId(file.getMongoId());
					for (FileContent child : childFiles) {
						String newPath = targetPath + file.getName() + "/";
						createChildFolder(child, newPath, newId.toString());

					}
					// Update the parent folder hasChild status
					targetedLocation.setHasChild(true);
					repository.save(targetedLocation);
				}
			}


// FROM HB: `copy` action has names[] in request params as well, can use it for deletion
//			if (actionType.equals(Action.move)) {
//				deleteFiles(files);
//			}

			FileContent[] updatedFiles = Arrays.stream(files).flatMap(file -> {
				FileContent[] existingFilesAfterUpdate = repository.findByParentId(file.getMongoId());
				return Stream.of(existingFilesAfterUpdate);
			}).toArray(FileContent[]::new);

			return updatedFiles;

		} catch (Exception error) {
			throw error;
		}
	}

	// ------- HELPER FUNCTIONS ------- //

	/**
	 * Checks if there is an existing folder of same name in the parent folder
	 * @param folderName folder name
	 * @param parentId mongoId of parent folder
	 * @return TRUE if there is an existing folder of same name | FALSE if not
	 * @throws Exception "Folder/File with same name exists"
	 */
	private boolean isFolderDuplicate(String folderName, String parentId) throws Exception {
		Query query = new Query(Criteria
				.where("parentId").is(parentId)
				.and("name").regex("^" + folderName.toLowerCase() + "$", "i"));
		if (mongoTemplate.findOne(query, FileContent.class) != null){
			return true;
		} else {
			throw new Exception("Folder/File with same name exists");
		}
	};


	/**
	 * Checks if parent folder still exists before performing operations
	 * @param parentId  mongoId of parent folder
	 * @param path /path/to/parent
	 * @return parentFolder
	 * @throws Exception "/path/to/parent does not exist."
	 */
	private FileContent isParentFolderPresent(String parentId, String path) throws Exception {
		FileContent parentFolder = repository.findById(new ObjectId(parentId)).orElse(null);
		if (parentFolder != null) {
			return parentFolder;
		} else {
			throw new Exception(String.format("%s does not exist.", path));
		}
	};



	private List<FileContent> getChildFolders(String filterPath) {
		Query query = new Query(Criteria.where("filterPath").regex("^" + filterPath));


		return mongoTemplate.find(query, FileContent.class);
	}

}
