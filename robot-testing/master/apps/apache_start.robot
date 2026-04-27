*** Settings ***
Documentation    Test for Apache web server
Library          SeleniumLibrary

*** Keywords ***
Navigate to Server 
    Open Browser    http://localhost:8080    Chrome

*** Test Cases ***
Check Apache is running
    Navigate to Server