/**
 *  _______  ________._.
 *  \      \ \_____  \ |
 *  /   |   \  _(__  < |
 * /    |    \/       \|
 * \____|__  /______  /_
 *        \/       \/\/
 *          - development
 *
 * Name: Channels DVR Driver
 * Version: 1.0.0
 * Author: n3! development
 * 
 * Description: Channels DVR Remote Control.
 *
 * Features: Allows remote control features.
 *
 * Download Links:
 *
 * Driver: https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Channels_DVR/channelsdvr.groovy
 *
 * README: https://github.com/dmike3/Hubitat/blob/master/Drivers/Channels_DVR/README.TXT
 *-------------------------------------------------------------------------------------------------------------------
 * Copyright 2020 n3! development
 * 
 * The following software is to be used "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. 
 *-------------------------------------------------------------------------------------------------------------------
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
 
    input name: "clientIP", type: "text",   title: "Channels DVR Client IP Address", required: true
    input name: "chanNumber", type: "number",   title: "Channel Number (Optional)", required: false
    input name: "seekSecs", type: "number",   title: "Seek Seconds (Optional)", required: false
    input name: "recordingID", type: "number",   title: "Recording ID to play (Optional)", required: false
    input name: "pollTime", type: "enum", title: "Poll Time", required: true, multiple: false, defaultValue: timeOptions[2], options: timeOptions
    input name: "logEnable", type: "bool",   title: "Enable debug logging", defaultValue: false, required: true
}

metadata {

    definition (
        
        name: "Channels DVR Driver",
        namespace: "n3!",
        author: "n3! development",
        importUrl: "https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Channels_DVR/channelsdvr.groovy") {       
        capability "Initialize"
        capability "Refresh"
        capability "Switch"
        
        attribute "status", "string"
        attribute "channel_number", "string"
        attribute "now_playing", "string"
        attribute "fav_channels", "string"
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
    state.version = "1.0.0"
    status = "Unavailable"
    favChans = "Unavailable"
    chanNum = "0"
    nowPlaying = "Unavailable"
    
    if(logEnable) log.debug "Refresh Called"
    
    // Getting Status
    
    def paramsStatus = [
        uri: "http://" + clientIP + ":57000/api/status/",
        requestContentType: "application/json",
        timeout : 15,
    ]

    try {
        httpGet(paramsStatus) { response ->
            sendEvent(name: "switch", value: "on")
            runIn(1, getInfo)
    }
    } catch(Exception e) {
        if(logEnable) log.debug "Cannot reach Channels DVR Client. Unable to poll status. Setting switch to off"
        off()
        // Update custom attributes
        
        updateDataValue("status", "$status")
        sendEvent(name: "status", value: status)
    
        updateDataValue("channel_number", "$chanNum")
        sendEvent(name: "channel_number", value: chanNum)
    
        updateDataValue("now_playing", "$nowPlaying")
        sendEvent(name: "now_playing", value: nowPlaying)
    
        updateDataValue("fav_channels", "$favChans")
        sendEvent(name: "fav_channels", value: favChans)

    }
    
}

def getInfo() {
 
    status = "Unavailable"
    favChans = "Unavailable"
    chanNum = "0"
    nowPlaying = "Nothing Playing"

    def currstate = device.currentState("switch").getValue()
    
    if(currstate == "on") {
        
    def paramsStatus = [
        uri: "http://" + clientIP + ":57000/api/status/",
        requestContentType: "application/json",
        timeout : 15,
    ]

    try {
        httpGet(paramsStatus) { response ->
            status = response.data.status
            if(logEnable) log.debug "Getting channels status"
            on()
    }
    } catch(Exception e) {
        if(logEnable) log.debug "Cannot reach Channels DVR Client. Unable to poll status. Setting switch to off"
        off()
    }    
   
    // Get current channel
    
    def paramsChannel = [
        uri: "http://" + clientIP + ":57000/api/status/",
        contentType: "application/json",
    ]

    try {
        httpGet(paramsChannel) { response ->
            chanNum = response.data.channel.number
            if(logEnable) log.debug "Getting current channel"
    }
    } catch(Exception e) {
        if(logEnable) log.debug "Cannot reach Channels DVR Client. Unable to poll current channel"
    }  
           
    // Get now playing name
    
    def paramsPlaying = [
        uri: "http://" + clientIP + ":57000/api/status/",
        contentType: "application/json",
    ]

    try {
        httpGet(paramsPlaying) { response ->
            nowPlaying = response.data.now_playing.title
            if(logEnable) log.debug "Getting now playing name"
    }
    } catch(Exception e) {
        if(logEnable) log.debug "Cannot reach Channels DVR Client. Unable to poll current channel"
    }  
    
    // Get favourite channel list
    
    def paramsFav = [
        uri: "http://" + clientIP + ":57000/api/favorite_channels/",
        contentType: "application/json",
    ]

    try {
        httpGet(paramsFav) { response ->
            favChans = response.data.name
            if(logEnable) log.debug "Getting favourite channel list"
    }
    } catch(Exception e) {
        if(logEnable) log.debug "Cannot reach Channels DVR Client. Unable to poll favourite channels"
    }    
    
} // Closing IF
    
    // Update custom attributes
    
    updateDataValue("status", "$status")
    sendEvent(name: "status", value: status)
    
    updateDataValue("channel_number", "$chanNum")
    sendEvent(name: "channel_number", value: chanNum)
    
    updateDataValue("now_playing", "$nowPlaying")
    sendEvent(name: "now_playing", value: nowPlaying)
    
    updateDataValue("fav_channels", "$favChans")
    sendEvent(name: "fav_channels", value: favChans)

}

// Commands

    command "toggle_mute"
    command "prev_channel"
    command "toggle_pause"
    command "pause"
    command "resume"
    command "stop"
    command "seek_secs"
    command "seek_forward"
    command "seek_backward"
    command "skip_forward"
    command "skip_backward"
    command "play_channel"
    command "play_recording"     

def on() {
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendEvent(name: "switch", value: "off")
    stop() 
}

// Event Handlers

def toggle_mute() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") { 
        
    def params = [
        uri: "http://" + clientIP + ":57000/api/toggle_mute/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending toggle mute command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"    
    }
       
}

def prev_channel() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
    def params = [
        uri: "http://" + clientIP + ":57000/api/previous_channel/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending previous channel command"
            runIn(1, refresh)
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }   
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"    
    }
}

def toggle_pause() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
    def params = [
        uri: "http://" + clientIP + ":57000/api/toggle_pause/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending toggle pause command"
    }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }    
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }    
}

def pause() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
        
    def params = [
        uri: "http://" + clientIP + ":57000/api/pause/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending pause command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"  
    }
}

def resume() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
    def params = [
        uri: "http://" + clientIP + ":57000/api/resume/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending resume command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }
}

def stop() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
    def params = [
        uri: "http://" + clientIP + ":57000/api/stop/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending stop command"
            runIn(1, refresh)
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }  
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }
}

def seek_secs() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
        if(seekSecs) {
    
            def params = [
            uri: "http://" + clientIP + ":57000/api/seek/$seekSecs",
            contentType: "application/json",
        ]

        try {
            httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending stop command"
        }
        } catch(Exception e) {
            if(logEnable) log.debug "error occured calling httpPost ${e}"
        }   
        
        }
        else {
            log.info "Seek Seconds not defined"   
        }
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }    
}

def seek_forward() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
        def params = [
            uri: "http://" + clientIP + ":57000/api/seek_forward/",
            contentType: "application/json",
        ]

        try {
            httpPostJson(params) { resp ->
                if(logEnable) log.debug "Sending seek forward command"
        }
        } catch(Exception e) {
            if(logEnable) log.debug "error occured calling httpPost ${e}"
        }   
        
    }
    
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }
}

def seek_backward() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
    def params = [
        uri: "http://" + clientIP + ":57000/api/seek_backward/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending seek backward command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }
        
    }
    else {
        if(logEnable) log.debugg "Channels is currently in the off state"   
    }
}

def skip_forward() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
    def params = [
        uri: "http://" + clientIP + ":57000/api/skip_forward/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending skip forward command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }
        
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }
}

def skip_backward() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
        
    def params = [
        uri: "http://" + clientIP + ":57000/api/skip_backward/",
        contentType: "application/json",
    ]

    try {
        httpPostJson(params) { resp ->
            if(logEnable) log.debug "Sending skip backward command"
        }
    } catch(Exception e) {
        if(logEnable) log.debug "error occured calling httpPost ${e}"
    }
    
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }
}

def play_channel() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
    
        if(chanNumber) {
        
        def params = [
            uri: "http://" + clientIP + ":57000/api/play/channel/$chanNumber",
            contentType: "application/json",
        ]

        try {
            httpPostJson(params) { resp ->
                if(logEnable) log.debug "Sending channel number command"
                runIn(1, refresh)
        }
        } catch(Exception e) {
            if(logEnable) log.debug "error occured calling httpPost ${e}"
        }   
    
        }
        else {
            log.info "Channel number not defined"   
        }
    }
    
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }
}

def play_recording() {
    
    currstate = device.currentState("switch").getValue()
    if(currstate == "on") {
        
        if(recordingID) {
        
        def params = [
            uri: "http://" + clientIP + ":57000/api/play/recording/$recordingID",
            contentType: "application/json",
        ]

        try {
            httpPostJson(params) { resp ->
                if(logEnable) log.debug "Sending recording ID command"
                runIn(1, refresh)
        }
        } catch(Exception e) {
            if(logEnable) log.debug "error occured calling httpPost ${e}"
        } 
        }
    
        else {
            log.info "Recording ID not defined"   
        }
    }
    else {
        if(logEnable) log.debug "Channels is currently in the off state"   
    }
}
