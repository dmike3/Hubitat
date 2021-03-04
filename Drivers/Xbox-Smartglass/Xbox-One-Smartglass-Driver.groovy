/**
 *  _______  ________._.
 *  \      \ \_____  \ |
 *  /   |   \  _(__  < |
 * /    |    \/       \|
 * \____|__  /______  /__
 *        \/       \/\/
 *          - development
 *
 * Name: Xbox One Smartglass Driver
 * Version: 1.0.3
 * Author: n3! development
 * 
 * Description: Xbox One Smartglass Intergration. The driver interfaces with the Xbox Smartglass Project giving 
 * local control of your Xbox One.
 *
 * Features: Powering On, Powering Off, Polling for state changes, sending controller button presses
 *
 * Download Links:
 *
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
 * June 24, 2020 - Updated timeout to not throw an error in the logs if it can't reach the specified xbox
 * June 12, 2020 - Added a front end for the driver. Parent/Child apps.
 * June 8, 2020 - Updated the default poll time. Included an App installer
 *
 **/

import groovy.transform.Field

@Field static List timeOptions = [
    "1 Minute",
    "3 Minutes",
    "5 Minutes",
    "10 Minutes",
    "15 Minutes",
    "30 Minutes",
    "1 Hour",
    
]


preferences {
 
    input name: "restIp", type: "text",   title: "REST IP Address", required: true
    input name: "restPORT", type: "text",   title: "REST Port Number", required: true
    input name: "xboxIP", type: "text",   title: "Xbox IP Address", required: true
    input name: "liveID", type: "text",   title: "Live ID", required: true
    input name: "pollTime", type: "enum", title: "Poll Time", required: true, multiple: false, defaultValue: timeOptions[2], options: timeOptions
    input name: "logEnable", type: "bool",   title: "Enable debug logging", defaultValue: false, required: true

}

metadata {

    definition (
        name: "Xbox One Smartglass Driver",
        namespace: "n3!",
        author: "n3! development",
        importUrl: "https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Xbox-Smartglass/Xbox-One-Smartglass-Driver.groovy") {
               
            capability "Initialize"
            capability "Switch"
            capability "Refresh"
        
        }
    }


def initialize(){
   
    if (pollTime == "1 Minute") {
        schedule("0 * * ? * *", refresh)
    }
    
    if (pollTime == "3 Minutes") {
        schedule("0 */3 * ? * *", refresh)
    }
    
    
    if (pollTime == "5 Minutes") {
        schedule("0 */5 * ? * *	", refresh)
    }
    
    if (pollTime == "10 Minutes") {
        schedule("0 */10 * ? * *", refresh)
    }
    
    if (pollTime == "15 Minutes") {
        schedule("0 */15 * ? * *", refresh)
    }
    
    if (pollTime == "30 Minutes") {
        schedule("0 */30 * ? * *", refresh)
    }
    
    if (pollTime == "1 Hour") {
        schedule("0 0 * ? * *", refresh)
    }
       
}

def updated() {
    unschedule(pollTime)
    initialize()
    
}

def refresh() {
 
    state.version = "1.0.3"
    
    if(logEnable) log.debug "Refresh Called"
    def xboxStatus = ""
    def currstate = device.currentState("switch").getValue()
    
    httpGet([uri:"http://${restIp}:$restPORT/device", timeout: 10]) { response -> 
            if (response.isSuccess())
            if(logEnable) log.debug "Polling Xbox Devices"
    } 
  
    def paramsStatus = [
        uri: "http://${restIp}:$restPORT/device?addr=$xboxIP",
        timeout : 10,
    ]

    // Checking Xbox Status
    
    try {
        httpGet(paramsStatus) { response ->
            xboxStatus = response.data[0].device_status // Storing Device Status in Variable
    }
    } catch(Exception e) {
        if(logEnable) log.debug "Cannot reach Xbox $xboxIP. Unable to poll status."
    }
    
    // End of checking Xbox Status
    
    // Setting Switch Status
       
    if(xboxStatus == "Available") {
      
        if(currstate == "off") {
            if(logEnable) log.debug "Setting Xbox $xboxIP to on"
            sendEvent(name: "switch", value: "on")
        }
        else {
            if(logEnable) log.debug "Skipping. Xbox $xboxIP is already on"
            
        }
        
    }
    else {
        
        if(currstate == "on") {
            if(logEnable) log.debug "Setting $xboxIP to off"
            sendEvent(name: "switch", value: "off")
        }
        else {
            if(logEnable) log.debug "Skipping. Xbox $xboxIP is already off"
            
        }
    }
          
}

// Commands

    command "clear"
    command "enroll"
    command "nexus"
    command "menu"
    command "view"
    command "a"
    command "b"
    command "x"
    command "y"
    command "dpad_up"
    command "dpad_down"
    command "dpad_left"
    command "dpad_right"
    command "left_shoulder"
    command "right_shoulder"
    command "left_thumbstick"
    command "right_thumbstick"
        

// Event Handlers

// *** Turning On ***

def on() {
    
    sendEvent(name: "switch", value: "on")
    def xboxStatus = ""
    
    httpGet([uri:"http://${restIp}:$restPORT/device", timeout: 10]) { response -> 
            if (response.isSuccess())
            if(logEnable) log.debug "Polling Xbox Devices"
        }
    
    // Checking Xbox Power State via REST
    httpGet([uri:"http://${restIp}:$restPORT/device?addr=$xboxIP"], { response ->
        xboxStatus = response.data[0].device_status // Storing Device Status in Variable
    })
   
    if(xboxStatus == "Available") {
        
        if(logEnable) log.debug "Not sending poweron commands. Xbox $xboxIP is reporting available. This could be because the Xbox is already on."
        log.info "Skipping. Xbox $xboxIP is already on"
 
        }
        
        else {
        
            httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/poweron?addr=$xboxIP", timeout: 10]) { response -> 
                if (response.isSuccess())
                    log.info "Turning on Xbox $xboxIP"
                }
        }
       
}
    
// *** Turning Off Xbox ***

def off() {

    sendEvent(name: "switch", value: "off")
    def xboxStatus = ""
    
    httpGet([uri:"http://${restIp}:$restPORT/device", timeout: 10]) { response -> 
            if (response.isSuccess())
            if(logEnable) log.debug "Polling Xbox Devices"
        }
    
 
    httpGet([uri:"http://${restIp}:$restPORT/device?addr=$xboxIP"], { response ->
    xboxStatus = response.data[0].device_status // Storing Device Status in Variable
    
    })
    
    if(xboxStatus == "Available") {
        if(logEnable) log.debug "The status of $xboxIP is $xboxStatus. Sending power off commands"    
        
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
            if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/poweroff?addr=$xboxIP", timeout: 10]) { response -> 
            if (response.isSuccess())
            log.info "Turning off Xbox $xboxIP"
            
        }
    
    } 
    
    else {
            if(logEnable) log.debug "Not sending poweroff commands. Xbox $xboxIP isn't reporting available. This could be because the Xbox is already off."
            log.info "Skipping. Xbox $xboxIP is already off"
    }
  	
}

// Xbox Buttons

def clear() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/clear", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Clear button was pressed"
           
    }
}

def enroll() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/enroll", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Enroll button was pressed"
           
    }
    
}

def nexus() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/nexus", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Nexus button was pressed"
           
    }
    
}

def menu() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
            if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/menu", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Menu button was pressed"
           
    }
    
}

def view() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/view", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "View button was pressed"
           
    }
    
}

def a() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/a", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "A button was pressed"
           
    }
    
}

def b() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/b", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "B button was pressed"
           
    }
    
}

def x() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/x", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "X button was pressed"
           
    }
    
}

def y() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/y", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Y button was pressed"
           
    }
    
}

def dpad_up() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/dpad_up", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Dpad_Up button was pressed"
           
    }
    
}

def dpad_down() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/dpad_down", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "dpad_down button was pressed"
           
    }
    
}

def dpad_left() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/dpad_left", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "dpad_left button was pressed"
           
    }
    
}

def dpad_right() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/dpad_right", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "dpad_right button was pressed"
           
    }
    
}

def left_shoulder() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/left_shoulder", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "left_shoulder button was pressed"
           
    }
    
}

def right_shoulder() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/right_shoulder", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "right_shoulder button was pressed"
           
    }
  
}

def left_thumbstick() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/left_thumbstick", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "left_thumbstick button was pressed"
           
    }
  
}

def right_thumbstick() {
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/input/right_thumbstick", timeout: 10]) { response -> 
        if (response.isSuccess())
            if(logEnable) log.debug "right_thumbstick button was pressed"
           
    }
  
}
