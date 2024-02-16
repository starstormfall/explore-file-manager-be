package com.explorer.filemanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="api/v1/FileOperations")
public class FileOperationController {

    // read
    @GetMapping
    public String readFolder() {
        return "Read Folder";
    }
    // create

    // delete

    // rename

    // search

    // details

    // copy

    // move

    // upload / save

    // download

    // getImage

}
