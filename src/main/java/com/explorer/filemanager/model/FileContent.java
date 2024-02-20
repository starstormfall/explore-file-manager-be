package com.explorer.filemanager.model;

import com.explorer.filemanager.dto.AccessRules;
import jakarta.annotation.Nullable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("products")

/**
 * known as FileManagerDirectoryContent by Syncfusion React File Manager
 */
public class FileContent {

    @Id
    private String id;
    // --- CORE --- //
    private String name; // file name
    private String dateCreated; //UTC Date string
    private String dateModified; //UTC Date string
    private String filterPath; // relative path to file or folder
    private Boolean hasChild; // whether folder has child
    private Boolean isFile; // whether item is file or folder
    private Number size; // file size e.g. 49792
    private String type; // file extension e.g. ".png",

    // --- Optional --- //
    @Nullable private AccessRules permission;
    @Nullable private Boolean caseSensitive; // defines if search is case sensitive
    @Nullable private String action; // name of file operation
    @Nullable private String[] names; // name list of items to be downloaded
////    @Nullable FileContent data; // details of downloaded item
//    @Nullable private List<MultipartFile> uploadFiles; // files that are uploaded > IList<IFormFile> (binary)
//    @Nullable private String newName; // new name for item
//    @Nullable private String searchString; // string to be searched in directory
//    @Nullable private String targetPath; // relative path where the items to be pasted are located
////    @Nullable FileContent targetData; // details of copied item
//    @Nullable private String[] renameFiles; //details of renamed item
//    @Nullable private String path;
//    @Nullable private String previousName;

}
