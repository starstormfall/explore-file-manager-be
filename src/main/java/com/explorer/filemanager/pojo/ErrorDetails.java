package com.explorer.filemanager.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorDetails {

    String code; // error code
    String message; // error message
    String[] fileExists; // List of duplicate file names
}
