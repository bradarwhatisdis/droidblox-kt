package com.drake.droidblox.shizuku;

interface IFileWriter {
    boolean writeFile(String path, String content);
    String execCommand(String command);
}
