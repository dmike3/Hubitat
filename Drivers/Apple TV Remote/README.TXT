_______  ________._.
  \      \ \_____  \ |
  /   |   \  _(__  < |
 /    |    \/       \|
 \____|__  /______  /_
        \/       \/\/
          - development

 *** GENERAL ***
 
 This driver works with Homebridge Apple TV Remote plugin via REST.
 
 You need to first setup and configure Homebridge and the homebridge plugin.
 Installation information can be found below.
 
 After completing and confirming the REST server is working, follow the instructions below.
 
 *** PREREQUISITES: ***
 
 - Homebridge: https://homebridge.io/
 - Homebridge Apple TV Remote: https://www.npmjs.com/package/homebridge-apple-tv-remote
 
 *** INSTALLATION: ***
 
 1.) Go to your Hubitat Drivers Code area and create a new driver.
 2.) Import the .groovy file either by using the import function or copy and pasting. 
 3.) Save the driver and go to your drivers section under Hubitat.
 4.) Create a new driver with a name of your choice "Living Room Apple TV" and select the driver "Apple TV Remote". Click Save.
 5.) Now fill in the paramaters to match your devices. (The unique name is the apple tv name you setup under homebridge.)
 6.) Consider picking a Poll Time that makes sense. This will make calls to your REST server to check if the Apple TV is on.
     This is required so it can track state changes if you turn the Apple TV on or off by hand. 
     
     
*** EXAMPLES/IDEAS: ***
 
- Bedtime routine to turn off Apple TV(s).
- Occupancy to trigger the Apple TV(s) to go off.
- Motion Sensor detects motion, turn Apple TV ON, select an App, and start playing content.

*** SUPPORT: ***
 
You can find me under the Hubitat forums if you have any questions.

*** Donation: ***

URL: https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=7TFJDAWDBTJL2&currency_code=USD&source=url
