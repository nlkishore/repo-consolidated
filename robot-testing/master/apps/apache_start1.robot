*** Settings ***
Documentation    Test for Apache web server
Library          SeleniumLibrary
*** Keywords ***
Navigate to Server 
    Open Browser    http://localhost:8080    Chrome

Verify Page is Apache Default Page
    Wait Until Page Contains Element    xpath://h2[contains(.,'Congratulations!')]

*** Test Cases ***

Check Apache is running
    Navigate to Server
    Verify Page is Apache Default Page