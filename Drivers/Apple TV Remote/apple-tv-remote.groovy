/**
 *  _______  ________._.
 *  \      \ \_____  \ |
 *  /   |   \  _(__  < |
 * /    |    \/       \|
 * \____|__  /______  /_
 *        \/       \/\/
 *          - development
 *
 * Name: Apple TV Remote (Homebridge Apple TV Remote)
 * Version: 1.0.0 (Alpha)
 * Author: n3! development
 * 
 * Description: Apple TV Remote. This interfaces with the homebridge-apple-tv-remote REST API Server.
 *
 * Features: Powering On, Powering Off, Polling for state changes, sending controller button presses
 *
 * DOWNLOAD LINKS
 *
 * Driver: https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Apple%20TV%20Remote/apple-tv-remote.groovy
 * 
 * Homebridge: https://homebridge.io/
 * Homebridge Apple TV Remote: https://www.npmjs.com/package/homebridge-apple-tv-remote
 *
 * README: 
 *-------------------------------------------------------------------------------------------------------------------
 * Copyright 2020 n3! development
 * 
 * The following software is to be used "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. 
 *-------------------------------------------------------------------------------------------------------------------
 *
 * CHANGE LOG:
 *
 * 6/10/2020 - Included Playing statues. Custom Attribute.
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
 
    input name: "appleTV", type: "text",   title: "HB Apple TV Unique Name:", required: true
    input name: "hbIP", type: "text",   title: "HB IP Address:", required: true
    input name: "hbPORT", type: "text",   title: "HB REST Port:", required: true
    input name: "hbToken", type: "text",   title: "HB Apple TV Token:", required: true
    input name: "pollTime", type: "enum", title: "Poll Time", required: true, multiple: false, defaultValue: timeOptions[2], options: timeOptions    
    input name: "logEnable", type: "bool",   title: "Enable debug logging", defaultValue: false, required: true

}

metadata {

    definition (
        name: "Apple TV Remote",
        namespace: "n3!",
        author: "n3! development",
        importUrl: "https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Apple%20TV%20Remote/apple-tv-remote.groovy") {
               
        capability "Initialize"
        capability "Switch"
        capability "Refresh"
        
        attribute "playing", "string"
        
        }
    }


def initialize(){
    
    state.Version = '1.0.0 (Alpha)'
  
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
    
    if(logEnable) log.debug "Refresh Called"
    def appleTVStatus = ""
    def currstate = device.currentState("switch").getValue()
    
    // Checking Apple TV Status
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        requestContentType: "application/json",
        timeout : 15,
        headers: ['Authorization'  : hbToken]
    ]
    
    try {
    httpGet(params) { response ->
        appleTVStatus = response.data.isOn
        isPlaying = response.data.isPlaying
        if(logEnable) log.debug "$appleTV Status: $appleTVStatus"
        if(logEnable) log.debug "$appleTV Playing Status: $isPlaying"

    } 
    } catch(Exception e) {
        log.debug "error occured calling httpPost ${e}"
    }    
         
    // ---------------
    
    if(appleTVStatus == true) {
      
        if(currstate == "off") {
            if(logEnable) log.debug "Setting $appleTV to on"
            sendEvent(name: "switch", value: "on")
        }
        else {
            if(logEnable) log.debug "Skipping. $appleTV is already on"
        }
    }
    else if(appleTVStatus == false) {
        
        if(currstate == "on") {
            if(logEnable) log.debug "Setting $appleTV to off"
            sendEvent(name: "switch", value: "off")
        }
        else {
            if(logEnable) log.debug "Skipping. $appleTV is already off"    
        }
    }    
    
    if(isPlaying) {
        
        updateDataValue("playing", "Currently Playing")
        sendEvent(name: "playing", value: "Currently Playing")
    }
    else if(isPlaying == false) {
        
        updateDataValue("playing", "Idle")
        sendEvent(name: "playing", value: "Idle")
        
    }
          
}

// Commands
    
    command "up"
    command "down"
    command "left"
    command "right"
    command "menu"
    command "topmenu"
    command "home"
    command "play"
    command "pause"
    command "stop"
    command "next"
    command "previous"
    command "suspend"
    command "wake"
    command "volumeup"
    command "volumedown"
    command "select"
        

// Event Handlers

def on() {

    sendEvent(name: "switch", value: "on")
    wake()
    
}

def off() {
    
    sendEvent(name: "switch", value: "off")
    suspend()

}

def up() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"up","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending UP Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def down() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"down","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending DOWN Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def left() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"left","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending LEFT Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def right() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"right","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending RIGHT Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def menu() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : ('{"commands":[{"key":"menu","longPress": false}]}')
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending MENU Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }            

}

def topmenu() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"topmenu","longPress": false}]}'
    ]    

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending TOPMENU Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def home() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"home","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending HOME Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def play() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"play","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending PLAY Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def pause() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"pause","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending PAUSE Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def stop() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"stop","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending STOP Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def next() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"next","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending NEXT Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def previous() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"previous","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending PREVIOUS Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def suspend() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"suspend","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending SUSPEND Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def wake() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"wake","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending WAKE Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def volumeup() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"volumeup","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending VOLUMEUP Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def volumedown() {
  
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization' : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"volumedown","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending VOLUMEDOWN Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}

def select() {
    
    def params = [
        uri: "http://" + hbIP + ":" + hbPORT + "/" + appleTV + "",
        headers: ['Authorization'  : hbToken],
        contentType: "application/json",
        body : '{"commands":[{"key":"select","longPress": false}]}'
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending SELECT Command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
}
