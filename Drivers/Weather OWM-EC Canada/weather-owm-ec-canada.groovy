/**
 *  _______  ________._.
 *  \      \ \_____  \ |
 *  /   |   \  _(__  < |
 * /    |    \/       \|
 * \____|__  /______  /_
 *        \/       \/\/
 *          - development
 *
 * Name: Weather OWM-EC Canada
 * Version: 1.0 (Beta)
 * Author: n3!
 * 
 * Description: Polls weather information from OpenWeatherMap and Weather Environment Canada (Alert RSS Feed - https://weather.gc.ca/).
 *
 * Features: Current Weather and Canadian Weather Alerts
 *
 * Driver: 
 *
 * README: 
 *
 *-------------------------------------------------------------------------------------------------------------------
 * Copyright 2020 n3! development
 * 
 * The following software is to be used "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. 
 *-------------------------------------------------------------------------------------------------------------------
 **/


import groovy.transform.Field

@Field static List timeOptions = [

    "15 Minutes",
    "30 Minutes",
    "45 Minutes",
    "1 Hour",
]


@Field static List unitOptions = [

    "Celsius",
    "Fahrenheit",
]


preferences {
 
    input name: "rssFeed", type: "text",   title: "EC Weather Alert RSS Feed", required: true
    input name: "owmAPI", type: "text",   title: "OW API Key", required: true
    input name: "lat", type: "text",   title: "OW Latitude", required: true
    input name: "lon", type: "text",   title: "OW Longtitude", required: true
    input name: "units", type: "enum", title: "Unit Setting", required: true, multiple: false, defaultValue: unitOptions[0], options: unitOptions
    input name: "pollTime", type: "enum", title: "Poll Time", required: true, multiple: false, defaultValue: timeOptions[1], options: timeOptions
    input name: "logEnable", type: "bool",   title: "Enable debug logging", defaultValue: false, required: true
    

}

metadata {
    
    definition (name: "Weather OWM-EC Canada", namespace: "n3!", author: "Mike Fenton", importUrl: "") {
    capability "Refresh"
    capability "Initialize"
    capability "Temperature Measurement"
    capability "RelativeHumidityMeasurement"
    capability "Pressure Measurement"

    attribute "alert", "string"
    attribute "city", "string"
    attribute "summary", "string"
    attribute "feels_like", "number"
    attribute "temp_min", "number"
    attribute "temp_max", "number"
    attribute "windSpeed", "number"
    attribute "windDirection", "number"
    attribute "visibility", "number"
    attribute "clouds", "number"
    attribute "country", "string"
    attribute "sunRise", "string"
    attribute "sunSet", "string"
       
    }
}


def initialize(){
   
    getWeather()
    
    if (pollTime == "15 Minutes") {
        schedule("0 */15 * ? * *", getWeather)
    }
    
    if (pollTime == "30 Minutes") {
        schedule("0 */30 * ? * *", getWeather)
    }
    
    if (pollTime == "45 Minutes") {
        schedule("0 */45 * ? * *", getWeather)
    }
    
    if (pollTime == "1 Hour") {
        schedule("0 0 * ? * *", getWeather)
    }
    
          
}

def updated() {
    unschedule(pollTime)
    initialize()
    
}

def refresh() {
    unschedule(pollTime)
    initialize()
        
}

def poll() {
    unschedule(pollTime)
    initialize()
        
}


// Commands

    command "poll"

// Event Handlers


def getWeather() {
    
    log.info "Polling Weather"
    
    // State Variables
    
    state.Application = 'Weather OWM-EC Canada'
    state.Version = '1.0'
    state.Author = "n3! development"
    
    // Parse Units

    if(units == "Celsius") {
         unitsParsed = "metric"
    }
    else {
         unitsParsed = "imperial"   
    }

    // Gets SunRise and SunSet Information from Hub
    def riseAndSet = getSunriseAndSunset()
    updateDataValue("sunRise", "$riseAndSet.sunrise")
    updateDataValue("sunSet", "$riseAndSet.sunset")
    sendEvent(name: "sunRise", value: riseAndSet.sunrise)
    sendEvent(name: "sunSet", value: riseAndSet.sunset)
    
    ec()
    ow()
    
}

// Polls OpenWeatherMap

def ow() {
    
    
    httpGet([uri:"http://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$owmAPI&units=$unitsParsed"], { response ->
         
        
        cityPoll = response.data.name
        updateDataValue("city", "$cityPoll")
        sendEvent(name: "city", value: cityPoll)

        summaryPoll = response.data.weather.description
        updateDataValue("summary", "$summaryPoll")
        sendEvent(name: "summary", value: summaryPoll)
        
        tempPoll = response.data.main.temp
        updateDataValue("temperature", "$tempPoll")
        sendEvent(name: "temperature", value: tempPoll)
        
        feelsLikePoll = response.data.main.feels_like
        updateDataValue("feels_like", "$feelsLikePoll")
        sendEvent(name: "feels_like", value: feelsLikePoll)
        
        tempMinPoll = response.data.main.temp_min
        updateDataValue("temp_min", "$tempMinPoll")
        sendEvent(name: "temp_min", value: tempMinPoll)
        
        tempMaxPoll = response.data.main.temp_max
        updateDataValue("temp_max", "$tempMaxPoll")
        sendEvent(name: "temp_max", value: tempMaxPoll)
        
        tempMaxPoll = response.data.main.temp_max
        updateDataValue("temp_max", "$tempMaxPoll")
        sendEvent(name: "temp_max", value: tempMaxPoll)
        
        pressurePoll = response.data.main.pressure
        updateDataValue("pressure", "$pressurePoll")
        sendEvent(name: "pressure", value: pressurePoll)
        
        humidityPoll = response.data.main.humidity
        updateDataValue("humidity", "$humidityPoll")
        sendEvent(name: "humidity", value: humidityPoll)
        
        visibilityPoll = response.data.visibility
        updateDataValue("visibility", "$visibilityPoll")
        sendEvent(name: "visibility", value: visibilityPoll)
        
        windSpeedPoll = response.data.wind.speed
        updateDataValue("windSpeed", "$windSpeedPoll")
        sendEvent(name: "windSpeed", value: windSpeedPoll)
        
        windDirectionPoll = response.data.wind.deg
        updateDataValue("windDirection", "$windDirectionPoll")
        sendEvent(name: "windDirection", value: windDirectionPoll)
        
        cloudsPoll = response.data.clouds.all
        updateDataValue("clouds", "$cloudsPoll")
        sendEvent(name: "clouds", value: cloudsPoll)
        
        countryPoll = response.data.sys.country
        updateDataValue("country", "$countryPoll")
        sendEvent(name: "country", value: countryPoll)
  
    })
    
}


// Polls Weather Environment Canada Alert Information

def ec() {
   
     updateDataValue("alert", "")
 
        httpGet([uri:"${rssFeed}"], { response ->
        
        alertPoll = response.data.entry.summary[0]
            if(logEnable) log.debug "Polled Weather Alert Information: $alertPoll"
  
    })
    
    updateDataValue("alert", "$alertPoll")
    sendEvent(name: "alert", value: alertPoll)
   
    
}
