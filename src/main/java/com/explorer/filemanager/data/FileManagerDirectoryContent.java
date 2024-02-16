package com.explorer.filemanager.data;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileManagerDirectoryContent {

    // --- CORE --- //
    String name; // file name
    String dateCreated; //UTC Date string
    String dateModified; //UTC Date string
    String filterPath; // relative path to file or folder
    Boolean hasChild; // whether folder has child
    Boolean isFile; // whether item is file or folder
    Number size; // file size e.g. 49792
    String type; // file extension e.g. ".png",

    // --- Optional --- //
    AccessRules permission;
    Boolean caseSensitive; // defines if search is case sensitive
    String action; // name of file operation
    List names; // name list of items to be downloaded
    FileManagerDirectoryContent data; // details of downloaded item
    List<MultipartFile> uploadFiles; // files that are uploaded > IList<IFormFile> (binary)
    String newName; // new name for item
    String searchString; // string to be searched in directory
    String targetPath; // relative path where the items to be pasted are located
    FileManagerDirectoryContent targetData; // details of copied item
    String[] renameFiles; //details of renamed item

    String path;

    String previousName;

}
