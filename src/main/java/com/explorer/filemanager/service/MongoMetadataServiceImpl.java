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
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	 *
	 * @param fileId mongoId
	 * @return metadata of current working directory ELSE null if file does not exist
	 */
	@Override
	public FileContent getCwd(String fileId) {
		return repository.findByMongoId(fileId);
	}

	/**
	 * Get all files with parentId
	 *
	 * @param parentId mongoId of parent folder
	 * @return list of child files' metadata belonging to a parent
	 */
	@Override
	public List<FileContent> getFilesByParentId(String parentId) {
		return repository.findByParentId(parentId);
	}


	/**
	 * creates a new document (folder entry)
	 * updates parent folder to hasChild so that nested folder can show up in frontend tree view
	 *
	 * @param folderName name of new folder to be created
	 * @param parentId   mongoId of parent folder to be created in
	 * @param path       /path/to/parent
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
	 *
	 * @param file    selected file
	 * @param newName new name
	 * @return renamed file
	 * @throws Exception if parent folder does not exist
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
				String oldDescendantFilterPath = file.getFilterPath() + oldName + "/";
				String newDescendantFilterPath = file.getFilterPath() + newName + "/";
				List<String> descendantFileIds = getDescendantFileIds(oldDescendantFilterPath);
				updateDescendantFilterPath(descendantFileIds, oldDescendantFilterPath, newDescendantFilterPath);
			}
			return repository.save(file);
		} else {
			throw new Exception("Something went wrong...");
		}
	}

	/**
	 * Delete files given a list of filenames
	 * Also deletes the nested folders and files
	 *
	 * @param names list of folder names
	 * @param files list of folder details
	 * @return list of remaining files with same parentId after deletion
	 */
	@Override
	public List<FileContent> deleteFiles(String[] names, List<FileContent> files) {
		for (FileContent file : files) {
			// get all children folders of selected file and remove them first
			List<String> childrenFolderIds = getDescendantFileIds(file.getFilterPath() + file.getName()+"/");
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
		if (existingFilesAfterDeletion.isEmpty()) {
			parentFolder.setHasChild(false);
		}
		parentFolder.setDateModified(String.valueOf(Instant.now()));
		repository.save(parentFolder);

		return existingFilesAfterDeletion;
	}


	/**
	 * Searches the current working directory for files/folders with
	 *
	 * @param searchString received by frontend by default between *: "*searchString*"
	 * @param filterPath   current working directory
	 * @return list of matched files/folders
	 */
	@Override
	public List<FileContent> searchFiles(String searchString, String filterPath) {
		String sanitizedSearchString = searchString.substring(1, searchString.length() - 1);
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
	 * Creates new copy of files/folders from existing location to new location
	 *
	 * @param names       list of file/folder names to create a copy of
	 * @param files       list of file/folder contents to create a copy of
	 * @param oldPath     existing filterPath
	 * @param targetPath  new destination filterPath
	 * @param targetData  new destination filterPath
	 * @param renameFiles list of file/folder names to be renamed because same name already exists
	 * @return list of new file/folder content successfully copied
	 * @throws Exception if parent folder no longer exists
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public List<FileContent> copyFiles(String[] names, List<FileContent> files, String oldPath, String targetPath, FileContent targetData, String[] renameFiles) throws Exception {
		getParentFolder(files.get(0).getParentId(), files.get(0).getFilterPath());
		boolean isRename = renameFiles.length > 0;
		List<FileContent> copiedFiles = new ArrayList<>();

		for (FileContent file: files) {
			// create new parent copy
			FileContent newParentCopy = createNewCopy(isRename, file, targetPath, targetData.getMongoId());
			copiedFiles.add(newParentCopy);

			String parentId = newParentCopy.getMongoId();
			String newPath = newParentCopy.getFilterPath() + newParentCopy.getName() + "/";

			List<FileContent> children = repository.findByParentId(file.getMongoId());
			copyChildrenRecursively(children, newPath, parentId);
		}
		return copiedFiles;
	}

	/**
	 * Moves (cuts & pastes) files/folders from existing location to new location
	 *
	 * @param names       list of file/folder names to move
	 * @param files       list of file/folder contents to move
	 * @param oldPath     existing filterPath
	 * @param targetPath  new destination filterPath
	 * @param targetData  new destination filterPath
	 * @param renameFiles list of file/folder names to be renamed because same name already exists
	 * @return list of new file/folder content successfully moved
	 * @throws Exception if parent folder no longer exists
	 */
	@Override
	public List<FileContent> moveFiles(String[] names, List<FileContent> files, String oldPath, String targetPath, FileContent targetData, String[] renameFiles) throws Exception {
		getParentFolder(files.get(0).getParentId(), files.get(0).getFilterPath());
		boolean isRename = renameFiles.length > 0;

		for (FileContent file: files) {

			// TODO: allow for rename if file/folder of same name exists, throw exception?
			if (isFolderDuplicate(file.getName(), file.getParentId())) {}

			if (isRename) {
				String fileName = renameFileWithIncrementNumber(file.getName());
				file.setName(fileName);
			}
			// update parentId & filterPath
			file.setParentId(targetData.getMongoId());
			file.setFilterPath(targetPath);
			if (!folderHasChild(file.getMongoId())) {
				file.setHasChild(false);
			}
			repository.save(file);

			List<String> descendantIds = getDescendantFileIds(oldPath + file.getName() + "/");
			updateDescendantFilterPath(descendantIds, oldPath, targetPath);
		}
		return files;
	}


	// ------- HELPER FUNCTIONS ------- //

	/**
	 * Checks if there is an existing folder of same name in the parent folder
	 *
	 * @param folderName folder name
	 * @param parentId   mongoId of parent folder
	 * @return TRUE if there is an existing folder of same name | FALSE if not
	 */
	private boolean isFolderDuplicate(String folderName, String parentId) {
		Query query = new Query(Criteria.where("parentId").is(parentId).and("name").is(folderName));
		return mongoTemplate.findOne(query, FileContent.class) != null;
	}

	/**
	 * Checks if parent folder still exists before performing operations
	 *
	 * @param parentId mongoId of parent folder
	 * @param path     /path/to/parent
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
	}

	/**
	 * Gets mongoIds of all children and children's folder with same parent path (filterPath)
	 *
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
	 * Checks if a folder has descendants
	 *
	 * @param mongoId mongoId of folder
	 * @return true if folder has descendants
	 */
	private boolean folderHasChild(String mongoId) {
		return repository.findByParentId(mongoId) != null;
	}

	/**
	 * Adds backslash to escape special characters
	 *
	 * @param string relative path of the child folder
	 * @return sanitized string suitable for regex query
	 */
	private String sanitizeForRegex(String string) {
		// TODO: Fix bug where special characters like ( ) [ ] ** throws error
		String specialCharacters = "[()*],";
		String sanitizedString = string.replaceAll(specialCharacters, "\\\\$0");
		log.info(String.format("string: %s", string));
		log.info(String.format("sanitized: %s", sanitizedString));
		return sanitizedString;
	}

	/**
	 * Updates descendants filter path given a list of descendants' ids
	 *
	 * @param descendantIds list of descendant mongoIds
	 * @param oldPath old filterPath
	 * @param newPath new filterPath
	 */
	private void updateDescendantFilterPath(List<String> descendantIds, String oldPath, String newPath) {
		for (String descendantId : descendantIds) {
			FileContent descendant = repository.findByMongoId(descendantId);
			String oldFilterPath = descendant.getFilterPath();
			String newFilterPath = oldFilterPath.replaceAll(oldPath, newPath);
			descendant.setFilterPath(newFilterPath);
			descendant.setDateModified(String.valueOf(Instant.now()));
			repository.save(descendant);
		}
	}

	/**
	 * Renames file/folder name by incrementing version if same name exists for copy, move actions
	 *
	 * @param fileName existing file name
	 * @return new file name
	 */
	private String renameFileWithIncrementNumber(String fileName) {
		Pattern pattern = Pattern.compile("\\((\\d+)\\)");
		Matcher matcher = pattern.matcher(fileName);

		if(matcher.find()) {
			int number = Integer.parseInt(matcher.group(1));
			int incrementedNumber = number + 1;
			fileName = matcher.replaceAll("("+ incrementedNumber+")");
		} else  {
			fileName += "(1)";
		}
		return fileName;
	}

	/**
	 * Creates new copy of existing file
	 *
	 * @param file FileContent of existing file
	 * @param filterPath new filterPath
	 * @param parentId mongoId of parent folder
	 * @return FileContent of new copy
	 */
	private FileContent createNewCopy(boolean isRename, FileContent file, String filterPath, String parentId) {
		String fileName = file.getName();

		if (isRename) {
			renameFileWithIncrementNumber(fileName);
		}
		ObjectId id = new ObjectId();
		FileContent newCopy =  new FileContent(
				id.toString(),
				fileName,
				fileName,
				Instant.now().toString(),
				Instant.now().toString(),
				filterPath,
				file.getHasChild(),
				file.getIsFile(),
				file.getSize(),
				file.getType(),
				parentId // parentId
		);
        return repository.save(newCopy);
	}


	/**
	 * Creates new copy of children files/folders recursively
	 *
	 * @param files list of children files to delve into
	 * @param filterPath new filterPath from parent copy
	 * @param parentId new parentId from parent copy
	 */
	public void copyChildrenRecursively(List<FileContent> files, String filterPath, String parentId) {
		for (FileContent file: files) {
			FileContent parentCopy = createNewCopy(false, file, filterPath, parentId);
			if (file.getHasChild()) {
				String parentCopyId = parentCopy.getMongoId();
				String newPath = parentCopy.getFilterPath() + parentCopy.getName() + "/";
				List<FileContent> children = repository.findByParentId(file.getMongoId());
				copyChildrenRecursively(children, newPath, parentCopyId);
			}
			repository.save(parentCopy);
		}
	}
}