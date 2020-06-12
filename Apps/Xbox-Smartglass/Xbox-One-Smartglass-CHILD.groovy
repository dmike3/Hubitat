/**
 *  _______  ________._.
 *  \      \ \_____  \ |
 *  /   |   \  _(__  < |
 * /    |    \/       \|
 * \____|__  /______  /_
 *        \/       \/\/
 *          - development
 *
 * Name: Xbox One Smartglass Driver
 * Version: 1.0.2
 * Author: n3! development
 * 
 * Description: Xbox One Smartglass Intergration. The application and driver interfaces with the Xbox Smartglass Project
 * giving local control of your Xbox One.
 *
 * Features: Powering On, Powering Off, Polling for state changes, sending controller button presses
 *
 * Download Links:
 *
 * App: 
 * Driver: https://github.com/dmike3/Hubitat/blob/master/Drivers/Xbox-Smartglass/Xbox-One-Smartglass-Driver.groovy
 * Xbox One Smartglass Project: https://pypi.org/project/xbox-smartglass-core/
 *
 * README: https://github.com/dmike3/Hubitat/blob/master/Drivers/Xbox-Smartglass/README.TXT
 *-------------------------------------------------------------------------------------------------------------------
 * Copyright 2020 n3! development
 * 
 * The following software is to be used "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. 
 *-------------------------------------------------------------------------------------------------------------------
 *
 * CHANAGE LOG:
 *
 * June 12, 2020 - Added a front end for the driver. Parent/Child apps.
 * June 8, 2020 - Updated the default poll time. Included an App installer
 *
 **/

definition(
    name: "Xbox One Smartglass Child",
    namespace: "n3!",
    author: "n3! development",
    description: "Xbox One Smartglass Child",
    category: "Integration",
    parent: "n3!:Xbox One Smartglass",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",)


preferences {
    page(name: "mainPage")
    
        
    }

def mainPage(){
    dynamicPage(name: "mainPage", nextpage: null, install: true, uninstall: true, refreshInterval: 0) {

        section("<h1 style='color:green;font-weight: bold'><img src='https://github.com/dmike3/Hubitat/blob/master/Apps/Xbox-Smartglass/_graphics/xbox.png?raw=true'> Xbox One Smartglass</h1>", hideable: flase, hidden: false) {
        }           
  
        section("<div style='color:#ffffff;font-weight: bold;background-color:green;border: 1px solid;box-shadow: 2px 3px #A9A9A9'> Information:</div>") {
            paragraph "SmartGlass is a remote control protocol developed by Microsoft for their Xbox gaming system. This application interfaces with the Xbox Smartglass Project which allows local control of your Xbox One. <br><br>Please remember to install and setup the REST server. See <b>https://pypi.org/project/xbox-smartglass-core/</b> for more details.</br>"
        }  
        
        section("<div style='color:#ffffff;font-weight: bold;background-color:green;border: 1px solid;box-shadow: 2px 3px #A9A9A9'> Xbox Configuration:</div>") {
            input "restIP", "text", required: true, multiple: false, title: "<b>REST Server IP Address:</b>"
            input "restPORT", "text", required: true, multiple: false, title: "<b>REST Server Port Number:</b>"
            input "xboxIP", "text", required: true, multiple: false, title: "<b>Xbox IP Address:</b>"
            input "xboxLiveID", "text", required: true, multiple: false, title: "<b>Xbox Live ID:</b>"
            input "xboxName", "text", required: true, multiplle: false, title: "<b>Xbox Name</b>"
        }
        
        section("<div style='color:#ffffff;font-weight: bold;background-color:green;border: 1px solid;box-shadow: 2px 3px #A9A9A9'> General:</div>") {
            label title: "Enter a label:", required: true
            input "logEnable", "bool", title: "Enable Debug Logging", description: "debugging", defaultValue:false, submitOnChange:true
        }      
    }    
}

// -----------------------------------------------------------------------

def installed() {
    if(logEnable) log.debug "Installed Application"
    createChildDevice() 
    
}

def updated() {
    updateChildDevice()
   
}

def initialize() {   
  	updateChildDevice()

}

def createChildDevice() {
    
    try {
            def check = getChildDevices()
            
            if(!check) {
                if(logEnable) "Creating new device..."
                child = addChildDevice("n3!", "Xbox One Smartglass Driver", "X1-$xboxIP", [name: "$xboxName ($xboxIP)", label: "$xboxName ($xboxIP)", isComponent: true])
                child.updateSetting("restIp", "$restIP")
                child.updateSetting("restPORT", "$restPORT")
                child.updateSetting("restIp", "$restIP")
                child.updateSetting("xboxIP", "$xboxIP")
                child.updateSetting("liveID", "$xboxLiveID")
                
            }
        } catch (e) { log.error "Error creating device: ${e}" }
}

def updateChildDevice() {

    child = getChildDevice("Xbox One ($xboxIP)")
    log.debug "$child"
  
    if(child) {
    
    child.updateSetting("restIp", "$restIP")
    child.updateSetting("restPORT", "$restPORT")
    child.updateSetting("restIp", "$restIP")
    child.updateSetting("xboxIP", "$xboxIP")
    child.updateSetting("liveID", "$xboxLiveID")
    
    }   
}
