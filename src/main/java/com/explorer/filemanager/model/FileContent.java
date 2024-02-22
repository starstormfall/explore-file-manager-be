package com.explorer.filemanager.model;

import com.explorer.filemanager.dto.AccessRules;
import jakarta.annotation.Nullable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force=true)
@RequiredArgsConstructor
@Document("FileContent")

/**
 * known as FileManagerDirectoryContent by Syncfusion React File Manager
 */
public class FileContent {

    @Id
    private final String id;
    // --- CORE --- //
    private final String name; // file name
    private final String dateCreated; //UTC Date string
    private final String dateModified; //UTC Date string
    private final String filterPath; // relative path to file or folder (one-level before), "\\" for first folder after root
    private final Boolean hasChild; // true if there is nested folder, false if not
    private final Boolean isFile; // whether item is file or folder
    private final Number size; // file size e.g. 49792
    private final String type; // file extension e.g. ".png",

    // --- Optional --- //
    @Nullable private AccessRules permission;
    @Nullable private Boolean caseSensitive; // defines if search is case sensitive
    @Nullable private String action; // name of file operation
    @Nullable private String[] names; // name list of items to be downloaded
    @Nullable FileContent data; // details of downloaded item
    @Nullable private String newName; // new name for item
    @Nullable private String searchString; // string to be searched in directory
    @Nullable private String targetPath; // relative path where the items to be pasted are located
    @Nullable FileContent targetData; // details of copied item
    @Nullable private String[] renameFiles; //details of renamed item
    @Nullable private String path;
    @Nullable private String previousName;



//    @Nullable private List<MultipartFile> uploadFiles; // files that are to be uploaded to minio > IList<IFormFile> (binary)
}
