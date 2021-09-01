## COMPSYS725 Assignment 1: SFTP - RFC913

### Submission

This project implements the Simple File Transfer Protocol described in RFC 913 (https://tools.ietf.org/html/rfc913). This is completed in Java, using Port 115 as the default.

Author: Suyash

## File Structure
The file structure of this project is as follows:

- repo-folder
    - sftp
        - src
            - client
                - Client.java
            - server
                - Server.java
                - User.java
            - sftp
                - DatabaseConnection.java
                - userInfo.db
            - testing
                - TestProtocol.java 
            - testFiles  
        - sqlite-jdbc-3.30.1.jar (needed for SQLite Database Connection)

## Client Commands
Provided below is the list of commands provided in the RFC913 Protocol.

    <command> : = <cmd> [<SPACE> <args>] <NULL>
    <cmd> : =  USER ! ACCT ! PASS ! TYPE ! LIST ! CDIR
             KILL ! NAME ! DONE ! RETR ! STOR
    <response> : = <response-code> [<message>] <NULL>
    <response-code> : =  + | - |   | !

In this implementation, the commands are NOT case sensitive. Therefore, both 'USER user1' and 'user user1' will yield the same result. To send a command, simply enter the command on the Client console, followed by the ENTER key.

If the cmd sent is not listed above, the server will respond with:
'Invalid Command. Please enter a valid command'

All commands except USER, ACCT and PASS require the Client to be authenticated. If a command is entered that required authentication and the client is not logged in, the server will respond with:
'Please login first to use this command.'

## User Details

There are 4 users provided with this project. Their details are:

|  User   |  Acct |  Pass  |   
| ------- |:-----:| ------:|
| user1   |       |        |
| user2   | acct2 |        |
| user3   | acct3 | pass3  |
| user4   |       | pass4  |

#### user1 is the superior user i.e. provides easiest login process
User Details are stored in an SQLite Database, and fetched when required from within the ServerConnection. If further testing is required, simply open the userInfo.db file using a Database Browser and add in additional users. 

## Instructions for Compiling

1. Open Eclipse IDE
2. Choose Working Directory to be the gcoc113 folder. NOTE: must be one folder above sftp\
2. File -> Import -> General -> Existing Projects into Workspace
3. Select root directory to be the Working Directory chosen in Step 2.
4. Import sftp project and click Finish
5. Right-click on the project->Properties->Java Build Path->Libraries->Add External JARS-> sqlite-jdbc jar provided
6. Select JUnit 5 -> Remove (path will be incorrect otherwise)
7. Select Add libary->JUnit->JUnit 5->Finish
8. Click 'Apply and Close' to add both these jars to the build path

## Running the Tests

#### NOTE: Must Run these tests BEFORE manually interacting between the server and client, otherwise the expected output will not match the received output and the tests will fail  

#### NOTE-2: These tests have been tested for WINDOWS only. Windows and Linux appear to have different file seperators, so it has only been run on WINDOWS. Test functionality on WINDOWS!

The testing for this implementation is done automatically, through JUnit Test Cases. These are located in src\testing\TestProtocol.java 

These tests mimic the client sending commands and use assertEquals() to ensure the server response is as expected.

Steps to run these tests:

1. Ensure the compilation instructions have been followed above.
2. Right click on Server.java and Run As -> Java Application
3. Right click on TestProtocol.java and Run As -> JUnit Test

This will bring up a JUnit window that will show 5/5 tests passed.

## Manually interacting between Server and Client

Once the compilation stpes provided above have been completed, the following steps should be followed:

1. Window -> Show View -> Console
2. Right click on Server.java and Run As -> Java Application
3. Right click on Client.java and Run As -> Java Application
4. Use the Client.java console to interact between the two

## Test Cases
Each command is tested in the six test cases discussed below. Along with this, all of the four different login variations mentioned above are tested. Which login variation is used is listed below the description for each test. 

1. test_user_login_and_CDIR_command()  
This test involves the user logging in using both an account and password. The password is first provided incorrectly, followed by the correct password. Once authenticated, user3 calls CDIR to change to the src/ directory, followed by LIST F to list the files present in the src/ directory.  
USER = user3

2. test_unauthenticated_commands()  
This test case indicates that aside from USER, PASS and ACCT, all other commands cannot be used without the user being logged in. The server responds to all commands prior to authentication requiring the user login first.  
USER = not provided

3. testCDIR_before_login()  
The test involves using the CDIR command before the user is logged in. The user calls CDIR src before providing a password. In this situation, they are prompted to log in first. Now once they enter their password, they can then call CDIR src, which will result in the current working directory changing to src\. This is confirmed with a LIST V call, to indicate the correct directory was reached.   
USER = user4

4. test_NAME_and_KILL_commands()  
This test uses the NAME command to rename a file in the src\testFiles\ directory from renameFile.txt to deleteFile.txt. Following this, renameFile.txt is attempted to be deleted, which results in an error due to a non-existent file. CDIR .. is called to indicate the capability of the system to move to the parent directory when supplied with '..'. Following this, the deleteFile.txt is deleted using a relative directory argument to the KILL command.  
USER = user1
    
5. test_RETR_command()  
This test uses the RETR command to receive the testFile.txt file from the src\testFiles\ directory. Initially, the STOP command is used to stop the transaction. This is followed immediately by SEND, resulting in an error due to no transaction currently being active. The second time RETR is called, SEND is the command that follows which results in testFile.txt being saved on the client side.  
USER = user2

-- Not Implemented -- Future Work
6. test_STOR_command()  
This test indicates the different functionalities of the STOR command. It begins with STOR OLD testImage.jpg, which writes over the old testImage.jpg file. Following this is STOR APP appendFile.txt, which appends the contents of appendFile.txt to src\testFiles\appendFile.txt. Finally, STOR NEW testImage.jpg stores a new generation of the already existing testImage.jpg as 0-testImage.jpg. LIST F is called to confirm the files were transferred to the server.  
USER = user1

7. test_invalid_commands()  
This test tries to send secondary commands before their pre-decessors i.e. SEND before RETR, TOBE before NAME, SIZE before STOR. All result in a response from the server requiring the original command first. It also attempts to STOR a file that does not exist on the client side, resulting in the request not being sent to the server.  
USER = user4

## Commands + Responses

Provided below are the client commands and expected server responses. The format is:
    
    Client Command
        Server Response

#### USER
Command Format:  
    USER user-id
    
##### Tests:
    USER user1
        !user1 logged in

    USER user2
        +User-id valid, send account

    USER user3
        +User-id valid, send account and password

    USER user4
        +User-id valid, send password

    Invalid user-id provided
    USER bob
        -Invalid user-id, try again
    
#### ACCT
Command Format:  
    ACCT account

##### Tests:
    Following (USER user2 command) 
    ACCT acct2
        !Account valid, logged in

    Following (USER user3 command)
    ACCT acct3
        +Account valid, send password

    Invalid Account Provided
    ACCT incorrectAcct
        -Invalid account, try again
    
#### PASS
Command Format:  
    PASS password

##### Tests:
    If both valid user-id and account have been entered:
    PASS pass3
        !Logged in

    If only valid user-id has been provided
    PASS pass3
        +Send account

    Invalid Password Provided
    PASS bob
        -Wrong password, try again
    
#### TYPE
Command Format:  
    TYPE {A | B | C}

##### Tests:
    TYPE A
        +Using Ascii Mode

    TYPE B
        +Using Binary Mode

    TYPE C
        +Using Continous Mode

    TYPE Z
        -Type not valid
    
#### LIST
Command Format:  
    LIST {F | V} directory-path

##### Tests:
    LIST F
        +<CURRENT_DIRECTORY>
        list-of-files

    LIST F src
        +<CURRENT_DIRECTORY\src>
        list-of-files

    LIST V
        +<CURRENT_DIRECTORY>
        list-of-files  Type:    Size:   LastModified:     Hidden:

#### CDIR
Command Format:  
    CDIR new-directory

##### Tests:
    CDIR src
        !Changed working dir to <CURRENT_DIRECTORY\src>

    CDIR non_existent_directory
        -Can't connect to directory because: Directory does not exist

#### KILL
Command Format:  
    KILL file-spec

##### Tests:
    KILL existingFile.txt
        +<CURRENT_DIRECTORY\existingFile.txt> deleted

    KILL nonExistingFile.txt
        -Not deleted because: File does not exist
    
#### NAME
Command Format:  
    NAME old-file-spec
    
    If '+' received, send:
        TOBE new-file-spec

##### Tests:
    NAME existingFile.txt
        +File exists
    TOBE newFile.txt
        <CURRENT_DIRECTORY\existingFile.txt> renamed to newFile.txt    

    Try to rename a non existent file
    NAME nonExistingFile.txt
        -Can't find nonExistingFile.txt
    
#### DONE
Command Format:  
    DONE

##### Tests:
    DONE
        +SKAT-736 closing connection
    
#### RETR   -- [Hasn't been implemented]
Command Format:  
    RETR file-spec

##### Tests:
    RETR existingFile.txt
        <size_of_file>

        Reply with:
        SEND
            existingFile.txt saved on local drive
        STOP
            +ok, RETR aborted
    
    RETR nonExistingFile.txt
        -File doesn't exist
    
#### STOR   -- [Hasn't been implemented]
Command Format:  
    STOR {NEW | OLD | APP } file-spec

##### Tests:
    STOR NEW aFile.txt

        If file exists:
        +File exists, will create new generation of file

        If file doesn't exist:
        +File does not exist, will create new file

        both followed by:
        +ok, waiting for file
        +Saved aFile.txt

    STOR OLD aFile.txt

        If file exists:
        +Will write over old file

        If file doesn't exist:
        +Will create new file

        both followed by:
        +ok, waiting for file
        +Saved aFile.txt

    STOR APP aFile.txt

        If file exists:
        +Will append to file

        If file doesn't exist:
        +Will create file

        both followed by:
        +ok, waiting for file
        +Saved aFile.txt
