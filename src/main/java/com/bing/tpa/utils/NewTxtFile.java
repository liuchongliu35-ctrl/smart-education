package com.bing.tpa.utils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NewTxtFile {
    public static File newFile(String dataString,String fileName) throws IOException {
//        src/main/resources/txt/作业数据.txt
        File file=new File("src/main/resources/txt/"+fileName+".txt");
        try(FileWriter writer=new FileWriter(file)){
            writer.write(dataString);
        }
        return file;
    }
}
