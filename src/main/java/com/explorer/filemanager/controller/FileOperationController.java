package com.explorer.filemanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="api/v1/FileOperations")
public class FileOperationController {

    // READ
    @GetMapping
    // request params: String action; String path; Boolean showHiddenItems; FileManagerDirectoryContent data
    public String readFolder(@RequestParam()) {
        return "Read Folder";
    }
    // response: FileManagerDirectoryContent cwd; FileManagerDirectoryContent[] files; ErrorDetails error;

    // CREATE
    // request params: String action; String path; String name; FileManagerDirectoryContent data
    // response: FileManagerDirectoryContent[] files; ErrorDetails error;


    // RENAME
    // request params: String action; String path; String name; String newName; FileManagerDirectoryContent data
    // response: FileManagerDirectoryContent[] files; ErrorDetails error;


    // DELETE
    // request params: String action; String path; String[] names; FileManagerDirectoryContent data
    // response: FileManagerDirectoryContent[] files; ErrorDetails error;

    // DETAILS
    // request params: String action; String path; String[] names; FileManagerDirectoryContent data
    // response: FileManagerDirectoryContent details; ErrorDetails error;

    // SEARCh



    // copy

    // move

    // upload / save

    // download

    // getImage

}
