package com.explorer.filemanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="api/v1/GetImage")
public class GetImageController {

    @GetMapping
    public String getImage() {
        return "Image received";
    }

}
