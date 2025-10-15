package com.mik.file;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class FileOutput {
    private String url;
}
