package com.explorer.filemanager.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileDetails {
    String name;
    String location;
    Boolean isFile;
    String size;
    String created;
    String modified;
    Boolean multipleFiles;
}
