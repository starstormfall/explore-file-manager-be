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

@Slf4j
@Service
public class MongoMetadataServiceImpl implements MongoMetadataService {

	enum Action {
		read, create, rename, delete, details, search, copy, move,
	}

	private FileContentRepository repository;

	private MongoTemplate mongoTemplate;

	@Autowired
	public MongoMetadataServiceImpl(FileContentRepository repository, MongoTemplate mongoTemplate) {
		this.repository = repository;
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * Get cwd by id
	 * 
	 * @param fileId
	 * @return
	 */
	@Override
	public FileContent getCwd(String fileId) {
		FileContent cwd = repository.findById(new ObjectId(fileId)).get();
		return cwd;
	}

	/**
	 * Get all files with parentId
	 * 
	 * @param parentId
	 * @return
	 */
	@Override
	public FileContent[] getFilesByParentId(String parentId) {
		FileContent[] files = repository.findByParentId(parentId);
		return files;
	}

	/**
	 * creates a new document (folder entry) updates parent folder to hasChild so
	 * that nested folder can show up in frontend tree view
	 * 
	 * @param folderName
	 * @param parentId
	 * @param path
	 * @return
	 */
	@Override
	public FileContent createFolder(String folderName, String parentId, String path) throws Exception {

		// need to check if folder with same name already exists with parentId
		// if already exists, throw error
		// else can create new folder
		Query query = new Query(
				Criteria.where("parentId").is(parentId).andOperator(Criteria.where("name").is(folderName)));

		if (mongoTemplate.findOne(query, FileContent.class) == null) {
			ObjectId id = new ObjectId();
			FileContent newFolder = new FileContent(id.toString(), folderName, folderName, Instant.now().toString(),
					Instant.now().toString(), path, false, false, 0, "", parentId // parentId
			);

			FileContent newFolderData = repository.save(newFolder);

			FileContent parentFolder = repository.findById(new ObjectId(parentId)).get();
			parentFolder.setHasChild(true);
			repository.save(parentFolder);

			return newFolderData;
		} else {
			throw new Exception("Folder with same name exists");
		}
	}

	/**
	 * Delete files given a list of filenames Also deletes the nested folders and
	 * files
	 * 
	 * @param files
	 * @return list of remaining files with same parentId after deletion
	 * @throws Exception
	 */
	@Override
	public FileContent[] deleteFiles(FileContent[] files) throws Exception {

		// all files will share same parentId because only files under the same parent
		// folder can be deleted together
		String parentId = files[0].getParentId();
		List<String> fileNames = new ArrayList<>();
		List<String> fileIds = new ArrayList<>();

		for (FileContent file : files) {
			fileNames.add(file.getName());
			fileIds.add(file.getMongoId());
		}

		log.info(fileNames.toString());
		log.info(fileIds.toString());

		Criteria criteria = new Criteria().orOperator(
				// get selected files with matching fileName and matching parentId
				Criteria.where("name").in(fileNames).andOperator(Criteria.where("parentId").is(parentId)),
				// get child of selected files with child's parentId that matches the mongoId of
				// the files to be deleted
				Criteria.where("parentId").in(fileIds));

		Query query = new Query(criteria);

		mongoTemplate.findAllAndRemove(query, FileContent.class);

		FileContent[] existingFilesAfterDeletion = repository.findByParentId(parentId);
		return existingFilesAfterDeletion;

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
				FileContent[] exisitingFiles = repository.findByFilterPath(selectedFile.getFilterPath());
				boolean isExist = false;
				if (exisitingFiles.length > 0) {
					for (FileContent existingFile : exisitingFiles) {
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
			if (actionType.equals(Action.move)) {
				deleteFiles(files);
			}

			FileContent[] updatedFiles = Arrays.stream(files).flatMap(file -> {
				FileContent[] existingFilesAfterUpdate = repository.findByParentId(file.getMongoId());
				return Stream.of(existingFilesAfterUpdate);
			}).toArray(FileContent[]::new);

			return updatedFiles;

		} catch (Exception error) {
			throw error;
		}
	}

}
