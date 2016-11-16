# Convert PDS to HTML
Adobe Photoshop File Formats Specification
www.adobe.com

Please see the following 

1) Photoshop file format (PSD) specification
http://www.adobe.com/devnet-apps/photoshop/fileformatashtml/#50577409_72092

2) java-psd-library an open source pure java psd format parser
https://code.google.com/archive/p/java-psd-library/

Clone of java-psd-library
https://github.com/inevo/java-psd-library

# Objective
To develop a sample java app on windows or android platform using the PSD parser library from github.

# User Interface
A command line or a quick GUI without spending much time on this aspect

You can use any typical Photoshop PSD file as a sample 
Sections as per the Photoshop File Format specification

1) File header
2) Color mode data
3) Image resources
4) Layer and mask information
5) Image data

The parser output would be a class objects 
java-psd-library/psd-parser/src/psd/parser/

# Expected result
To output a full report of these objects to a text file.

