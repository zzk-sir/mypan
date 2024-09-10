package com.zhang.mypan.dto;

import com.zhang.mypan.entity.FileInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class DownloadDTO {
    private String fileName;
    private String filePath;

    public DownloadDTO(FileInfo fileInfo) {
        this.fileName = fileInfo.getFileName();
        this.filePath = fileInfo.getFilePath();
    }
}
