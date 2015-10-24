package com.softeng206.vidivox.util;

import java.io.File;

/**
 * Created by jay on 19/10/15.
 * This class was created in order to reduce the overall length of the Controller.java class. There were several methods
 * within the Controller class that were purely for utility and error checking, so it made sense to create final classes
 * and access these methods via the static modifier.
 */
final public class FileNameChecker {
    private FileNameChecker(){

    }

    public static File returnCorrectAudioFile(File selectedFile){

        if (selectedFile == null){
            return null;
        } else {
            String parent = selectedFile.getParent();

            // If the file doesn't end with the correct audio extension, then we need to add it.
            // Upon doing so, a File with the correct file name will be returned.

            if (!selectedFile.getName().endsWith(".mp3")){
                String fileName = selectedFile.getName() + ".mp3";
                return new File(parent + "/" + fileName);
            }
            return selectedFile;
        }
    }

    public static File returnCorrectVideoFile(File selectedFile){
        if (selectedFile == null){
            return null;
        } else {
            String parent = selectedFile.getParent();

            // If the file doesn't end with the correct video extension, then we need to add it.
            // Upon doing so, a File with the correct file name will be returned.

            if (!selectedFile.getName().endsWith(".mp4")){
                String fileName = selectedFile.getName() + ".mp4";
                return new File(parent + "/" + fileName);
            }
            return selectedFile;
        }
    }
}
