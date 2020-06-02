 *** GENERAL ***
 
 This driver works with the Xbox Smartglass project which gives local control of your Xbox One.
 
 You need to first setup and configure the Xbox Smartglass REST server and confirm working before setting up the Hubitat driver. 
 The installation guide can be found on the project site. https://pypi.org/project/xbox-smartglass-core/
 
 After completing and confirming the REST server is working. You will add the driver to your driver codes section of Hubitat.
 
 
 
 *** PREREQUISITES: ***
 
 1.) Smartglass REST Server
 2.) Turn on the feature Instant-On on your Xbox. This allows for powering on. Otherwise you will see errors in your Hubitat logs. 
 
 *** INSTALLATION: ***
 
 1.) Go to your Hubitat Drivers Code area and create a new driver.
 2.) Import the .groovy file either by using the import function or copy and pasting. 
 3.) Save the driver and go to your drivers section under Hubitat.
 4.) Create a new driver with a name of your choice "Xbox One X" and select the driver "Xbox Smartglass Driver". Click Save.
 5.) Now fill in the paramaters to match your devices.
 6.) Consider picking a Poll Time that makes sense. This will make calls to your REST server to check if the Xbox is on.
     This is required so it can track state changes if you turn the Xbox on by hand. 
     
     
*** EXAMPLES/IDEAS: ***
 
You can initiate a manual refresh if needed. You can do this using a RM rule. Remember to choose custom attribute and select refresh.
This will force a check if the Xbox One is online or not if you don't want to wait for refresh schedule.

Obviouse use case, would be to include this in one of your app schedules. Possibly a bedtime routine or an occupany routine?

*** SUPPORT: ***
 
You can find me under the Hubitat forums if you have any questions.
 
 