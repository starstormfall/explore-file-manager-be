package com.explorer.filemanager.model;

import com.explorer.filemanager.pojo.AccessRules;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@Document("FileContent")

/**
 * known as FileManagerDirectoryContent by Syncfusion React File Manager
 */
public class FileContent  {


    @Id
    @NonNull private String mongoId;  // unique identifier of file/folder
    // --- CORE --- //
    @NonNull private String id; // to be same as file name - used by frontend to generate path
    @NonNull private String name; // file name
    @NonNull private String dateCreated; //UTC Date string
    @NonNull private String dateModified; //UTC Date string
    @NonNull private String filterPath; // relative path to file or folder (one-level before), "\\" for first folder after root
    @NonNull private Boolean hasChild; // true if there is nested folder, false if not
    @NonNull private Boolean isFile; // whether item is file or folder
    @NonNull private Number size; // file size e.g. 49792
    @NonNull private String type; // file extension e.g. ".png",
    @NonNull private String parentId; // for root folder, parentId would be bucketId

    // --- Optional --- //
    private AccessRules permission;
    private Boolean caseSensitive; // defines if search is case sensitive
    private String action; // name of file operation
    private String[] names; // name list of items to be downloaded
    private FileContent data; // details of downloaded item
    private String newName; // new name for item
    private String searchString; // string to be searched in directory
    private String targetPath; // relative path where the items to be pasted are located
    private FileContent targetData; // details of copied item
    private String[] renameFiles; //details of renamed item
    private String path;
    private String previousName;

//    @Nullable private List<MultipartFile> uploadFiles; // files that are to be uploaded to minio > IList<IFormFile> (binary)
}
