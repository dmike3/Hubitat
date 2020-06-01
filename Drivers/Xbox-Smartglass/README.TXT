/**
 * Name: Xbox One Smartglass Driver
 * Version: 1.0
 * Download: https://github.com/mike3y/Hubitat/blob/master/Drivers/Xbox-Smartglass/Xbox-One-Smartglass-Driver.groovy
 * Xbox One Smartglass REST API: https://github.com/openxbox/xbox-smartglass-core-python
 * Description: Xbox One Smartglass Intergration. The driver interfaces with the Xbox Smartglass Project giving 
 * local control of your Xbox One.
 *-------------------------------------------------------------------------------------------------------------------
 * Copyright 2020 Mike Fenton
 * 
 * The following software is to be used "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. 
 *-------------------------------------------------------------------------------------------------------------------
 **/
  
 *** BETA ***
 
 This driver is currently Beta. I plan to add more features in the near future.
 
 *** GENERAL ***
 
 This driver works with the Xbox Smartglass project. See the links above. 
 
 You need to first setup and configure the Xbox Smartglass REST server and confirm working before setting up the Hubitat driver. 
 The installation guide can be found on the project site.
 
 After completing and confirming the REST server is working. You will add the driver to your driver codes section of Hubitat.
 
 PREREQUISITES:
 
 1.) Smartglass REST Server
 2.) Turn on the feature Instant-On on your Xbox. This allows for powering on. Otherwise you will see errors in your Hubitat logs. 
 
 INSTALLATION:
 
 1.) Go to your Hubitat Drivers Code area and create a new driver.
 2.) Import the .groovy file either by using the import function or copy and pasting. 
 3.) Save the driver and go to your drivers section under Hubitat.
 4.) Create a new driver with a name of your choice "Xbox One X" and select the driver "Xbox Smartglass Driver". Click Save.
 5.) Now fill in the paramaters to match your devices.
 6.) Consider picking a Poll Time that makes sense. This will make calls to your REST server to check if the Xbox is available (Turned On).
     This is required so it can track state changes if you turn the Xbox on by hand. 
     
 SUPPORT:
 
 You can find me under the Hubitat forums if you have any questions.
 
 
