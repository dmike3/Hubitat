/**
 * Xbox One Smartglass Driver
 * Download: 
 * Smartglass API: https://github.com/openxbox/xbox-smartglass-core-python
 * Description: Xbox One Smartglass Intergration. It interfaces with the Xbox Smartglass Project giving local control
 * to your Xbox.
 * A driver to interface with the Xbox Smartglass Python API via REST.
 *-------------------------------------------------------------------------------------------------------------------
 * Copyright 2020 Mike Fenton
 * 
 * The following software is to be used "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. 
 *-------------------------------------------------------------------------------------------------------------------
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
    input "pollTime", "enum", required: false, multiple: false, title: "Poll Time", defaultValue: timeOptions[1], options: timeOptions
    input name: "logEnable", type: "bool",   title: "Enable debug logging", defaultValue: false, required: true

}

metadata {
    
    definition (name: "Xbox One Smartglass Driver", namespace: "n3!", author: "Mike Fenton") {
    capability "Initialize"
    capability "Switch"
    capability "Refresh"
        
    }
}


def initialize(){
      
   // log.debug "The current state is $currstate"
    
    if (pollTime == "1 Minute") {
        schedule("0 * * ? * *", refresh)
        if(logEnable) log.debug "Polled. Polling in another $pollTime"
    }
    
     if (pollTime == "3 Minutes") {
        schedule("0 */3 * ? * *", refresh)
        if(logEnable) log.debug "Polled. Polling in another $pollTime"
    }
    
    
    if (pollTime == "5 Minutes") {
        schedule("0 */5 * ? * *	", refresh)
        if(logEnable) log.debug "Polled. Polling in another $pollTime"
    }
    
    if (pollTime == "10 Minutes") {
        schedule("0 */10 * ? * *", refresh)
        if(logEnable) log.debug "Polled. Polling in another $pollTime"
    }
    
    if (pollTime == "15 Minutes") {
        schedule("0 */15 * ? * *", refresh)
        if(logEnable) log.debug "Polled. Polling in another $pollTime"
    }
    
    if (pollTime == "30 Minutes") {
        schedule("0 */30 * ? * *", refresh)
        if(logEnable) log.debug "Polled. Polling in another $pollTime"
    }
    
    if (pollTime == "1 Hour") {
        schedule("0 0 * ? * *", refresh)
        if(logEnable) log.debug "Polled. Polling in another $pollTime"
    }
       
}

def updated() {
    unschedule(pollTime)
    initialize()
    
}

def refresh() {
 
    if(logEnable) log.debug "Refresh Called"
    def xboxStatus = ""
    def currstate = device.currentState("switch").getValue()
    
  
    httpGet([uri:"http://10.1.1.11:5557/device?addr=$xboxIP"], { response ->
    xboxStatus = response.data.devices[liveID].device_status // Storing Device Status in Variable
    
    })
    
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


// Event Handlers


// *** Turning On ***

def on() {
    
    sendEvent(name: "switch", value: "on")
    def xboxStatus = ""
    
    // Checking Xbox Power State via REST
    httpGet([uri:"http://10.1.1.11:5557/device?addr=$xboxIP"], { response ->
        xboxStatus = response.data.devices[liveID].device_status // Storing Device Status in Variable
    })
   
    if(xboxStatus == "Available") {
        
        if(logEnable) log.debug "Not sending poweron commands. Xbox $xboxIP is reporting available. This could be because the Xbox is already on."
        log.info "Skipping. Xbox $xboxIP is already On"
 
        }
        
        else {
        
            httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/poweron?addr=$xboxIP", timeout: 10]) { response -> 
                if (response.isSuccess())
                    log.info "Turning On Xbox $xboxIP"
                }
        }
       
}
    
// *** Turning Off Xbox ***

def off() {

    sendEvent(name: "switch", value: "off")
    def xboxStatus = ""
 
    httpGet([uri:"http://10.1.1.11:5557/device?addr=$xboxIP"], { response ->
    xboxStatus = response.data.devices[liveID].device_status // Storing Device Status in Variable
    
    })
    
    if(xboxStatus == "Available") {
        if(logEnable) log.debug "The status of $xboxIP is $xboxStatus. Sending power off commands"    
        httpGet([uri:"http://${restIp}:$restPORT/device", timeout: 10]) { response -> 
            if (response.isSuccess())
            if(logEnable) log.debug "Polling Xbox Devices"
        }
    
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/connect?anonymous=true", timeout: 10]) { response -> 
            if (response.isSuccess())
            if(logEnable) log.debug "Connecting to Xbox $xboxIP"
           
        }
    
    httpGet([uri:"http://${restIp}:$restPORT/device/$liveID/poweroff?addr=$xboxIP", timeout: 10]) { response -> 
            if (response.isSuccess())
            log.info "Turning Off Xbox $xboxIP"
            
        }
    
    } 
    
    else {
            if(logEnable) log.debug "Not sending poweroff commands. Xbox $xboxIP isn't reporting available. This could be because the Xbox is already off."
            log.info "Skipping. Xbox $xboxIP is already off."
    }
  	
}
