*** Settings ***
Documentation    Test Movie API Service
Library          SeleniumLibrary

*** Keywords ***
Navigate to Server 
    Open Browser    http://localhost:2081/movies-app/api/movies/m0001    Chrome

*** Test Cases ***
Check Movie Service API is running
    Navigate to Server