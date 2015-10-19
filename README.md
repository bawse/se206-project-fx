#SOFTENG 206 Project - Jay Pandya.
JavaFX: Use only .mp4 files.

Icons retrieved from : https://www.google.com/design/icons/
The use of these icons is permitted under the Creative Commons License: https://creativecommons.org/licenses/by/4.0/

Note: Please us the Remote Linux Image on the UG4 Lab computers. (NOT THE BETA
VERSION). If you are using your own machine to run the jar file, then you will require
JVM 1.8. It is also possible that the following run instructions may not be applicable to your
machine. Therefore, you will need to discover the path to your jvm yourself, and run the jar accordingly.

HOW TO RUN VIDIVOX:
1) Using Terminal, change to the directory where the runnable JAR file is saved.\n
2) Enter the following command into the terminal to run the JAR file:
    /usr/lib/jvm/jre1.8.0_45/bin/java -jar se206-project-fx.jar

Notes:
1) Please select only .mp4 files when browsing for a video, as this assignment
uses JavaFX, rather than Swing + vlcj, for UI and media functionality.

2) The resizing of the video in the UG4 labs is not smooth, but smooth on our
own machines.

3) The effect of the "Cancel Preview" button is also delayed on the UG4
machines, similar to Assignment 2. However, it is immediately responsive when
run on our own machines.

4) When saving a file (whether it be a text-to-speech MP3 or a video), please
enter the full file name WITH the extension. E.g. "text2speech.mp3",
"videowithnewaudio.mp4".

5) The time label for video only works for mm:ss format, not for hh:mm:ss.

Known bug: Resizing the window prior to playing a video results in the failure of the MediaPlayer resizing to "fit to screen".