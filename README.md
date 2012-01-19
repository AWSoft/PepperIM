PepperIM: Personal P2P Extremely Reliable Instant Messenger
===========================================================
Copyright (C) 2011 Anton Pirogov, Felix Wiemuth

License
-------

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Description
-----------
PepperIM is an experimental approach to reliable and secure instant messaging.

Download
--------
### Git
    $ git clone git://github.com/AWSoft/PepperIM.git

### Binaries
https://github.com/AWSoft/PepperIM/downloads

### Source
https://github.com/AWSoft/PepperIM

Usage
-----
### Build jar
    $ ant

### Create Javadoc documentation:
    $ ant doc

### Run JUnit tests
    $ ant test

### Run application
    $ java -jar release/PepperIM.jar


How to set up a Netbeans project of PepperIM
--------------------------------------------
1. Choose "File" -> "New project" from Netbeans main menu
2. Select "Java" -> "Java Project with Existing Sources"
3. Configure step "Name and Location" of the wizard as you want
4. At page "Existing Sources" select "PepperIM/src/" for Source Package Folders and "PepperIM/test/" for Test Package Folders.
5. Click Finish
6. In your project structure, right-click on "Libraries" and choose "Add JAR/Folder...". In the dialog navigate to "PepperIM/lib", select all *.jar files and click "Open".
