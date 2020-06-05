 _______  ________._.
  \      \ \_____  \ |
  /   |   \  _(__  < |
 /    |    \/       \|
 \____|__  /______  /_
        \/       \/\/
          - development

 *** GENERAL ***
 
 The driver interfaces and polls data from both Environment Weather Canada and OpenWeatherMap. Alert information is only polled from EC.
 
 *** PREREQUISITES: ***
 
 1.) OpenWeatherMap API Key (Free) 
 2.) Alert RSS URL to your city via Environment Weather Canada (https://weather.gc.ca/). After picking your city, scroll down and select
     the RSS icon. Select the Alert RSS feed. Copy this URL and paste it into "EC Weather Alert RSS Feed" paramater under the driver.
 
 *** INSTALLATION: ***
 
 1.) Go to your Hubitat Drivers Code area and create a new driver.
 2.) Import the .groovy file either by using the import function or copy and pasting. 
 3.) Save the driver and go to your drivers section under Hubitat.
 4.) Create a new driver with a name of your choice "Weather" and select the driver "weather-owm-ec-canada". Click Save.
 5.) Now fill in the paramaters to match your devices.
 6.) Consider picking a Poll Time that makes sense. This will make calls to both OW and EC for weather information.
         
*** EXAMPLE/USE CASES ***

- Alert based off a weather alert. If it contains or doesn't contain a string, notifiy via pushover and/or TTS.
- Alert based off of temperature. If it exceeds a certain threshold, notifiy via pushover and/or TTS.
- Alert based off wind speed. If the wind exceeds a certain threshold, notifiy via pushover and/or TTS.

*** SUPPORT: ***
 
You can find me under the Hubitat forums if you have any questions.

*** Donation: ***

URL: https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=7TFJDAWDBTJL2&currency_code=USD&source=url
 
