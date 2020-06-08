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
 * Driver: https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Weather%20OWM-EC%20Canada/weather-owm-ec-canada.groovy
 *
 * README: https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Weather%20OWM-EC%20Canada/README.TXT
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
    
    "5 Minutes",
    "10 Minutes",
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
    input name: "lon", type: "text",   title: "OW Longitude", required: true
    input name: "units", type: "enum", title: "Unit Setting", required: true, multiple: false, defaultValue: unitOptions[0], options: unitOptions
    input name: "pollTime", type: "enum", title: "Poll Time", required: true, multiple: false, defaultValue: timeOptions[1], options: timeOptions
    input name: "logEnable", type: "bool",   title: "Enable debug logging", defaultValue: false, required: true
    

}

metadata {
      
   definition (
   name: "Weather OWM-EC Canada",
   namespace: "n3!",
   author: "n3! development",
   importUrl: "https://github.com/dmike3/Hubitat/blob/master/Drivers/Weather%20OWM-EC%20Canada/weather-owm-ec-canada.groovy") {
        
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
   attribute "rainDay1", "number"
   attribute "rainDay2", "number"
   attribute "rainDay3", "number"
       
    }
}


def initialize(){
   
    getWeather()
    
    if (pollTime == "5 Minutes") {
        schedule("0 */5 * ? * *", getWeather)
    }
    
    if (pollTime == "10 Minutes") {
        schedule("0 */10 * ? * *", getWeather)
    }
    
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
         
        
        // City
        
        cityPoll = response.data.name
        
        if(!cityPoll) {
            cityPoll = "Unavailable"
        }
                
        updateDataValue("city", "$cityPoll")
        sendEvent(name: "city", value: cityPoll)

        // Summary 
        
        summaryPoll = response.data.weather.description
        
        if(!summaryPoll) {
            summaryPoll = "Unavailable"
        }
            
        updateDataValue("summary", "$summaryPoll")
        sendEvent(name: "summary", value: summaryPoll)
        
        // Temperature
        
        
        tempPoll = response.data.main.temp
        
        if(!tempPoll) {
            tempPoll = "Unavailable"
        }
        updateDataValue("temperature", "$tempPoll")
        sendEvent(name: "temperature", value: tempPoll)
        
        // Feels Like
        
        feelsLikePoll = response.data.main.feels_like
        
        if(!feelsLikePoll) {
             feelsLikePoll = 0
        }
        
        updateDataValue("feels_like", "$feelsLikePoll")
        sendEvent(name: "feels_like", value: feelsLikePoll)
        
        // Temp Min
        
        tempMinPoll = response.data.main.temp_min
        
        if(!tempMinPoll) {
             tempMinPoll = 0   
        }
        
        updateDataValue("temp_min", "$tempMinPoll")
        sendEvent(name: "temp_min", value: tempMinPoll)
        
        // Temp Max
        
        tempMaxPoll = response.data.main.temp_max
        
        if(!tempMaxPoll) {
             tempMaxPoll = 0   
        }
        
        updateDataValue("temp_max", "$tempMaxPoll")
        sendEvent(name: "temp_max", value: tempMaxPoll)
        
        // Pressure
        
        pressurePoll = response.data.main.pressure
        
        if(!pressurePoll) {
             pressurePoll = 0   
        }
        
        updateDataValue("pressure", "$pressurePoll")
        sendEvent(name: "pressure", value: pressurePoll)
        
        // Humidity
        
        humidityPoll = response.data.main.humidity
        
        if(!humidityPoll) {
             humidityPoll = 0   
        }
          
        updateDataValue("humidity", "$humidityPoll")
        sendEvent(name: "humidity", value: humidityPoll)
        
        // Visibility
        
        visibilityPoll = response.data.visibility
        
        if(!visibilityPoll) {
             visibilityPoll = 0   
        }
        
        updateDataValue("visibility", "$visibilityPoll")
        sendEvent(name: "visibility", value: visibilityPoll)
        
        // Wind Speed
        
        windSpeedPoll = response.data.wind.speed
        if(!windSpeedPoll) {
            windSpeedPoll = 0   
        }
        
        updateDataValue("windSpeed", "$windSpeedPoll")
        sendEvent(name: "windSpeed", value: windSpeedPoll)
        
        // Wind Direction
        
        windDirectionPoll = response.data.wind.deg
        
        if(!windDirectionPoll) {
             windDirectionPoll = 0   
        }
        
        updateDataValue("windDirection", "$windDirectionPoll")
        sendEvent(name: "windDirection", value: windDirectionPoll)
        
        // Clouds
        
        cloudsPoll = response.data.clouds.all
        
        if(!cloudsPoll) {
             cloudsPoll = 0   
        }
        updateDataValue("clouds", "$cloudsPoll")
        sendEvent(name: "clouds", value: cloudsPoll)
        
        // Country Poll
        
        countryPoll = response.data.sys.country
        
        if(!countryPoll) {
             countryPoll = "Unavailable"   
        }
        updateDataValue("country", "$countryPoll")
        sendEvent(name: "country", value: countryPoll)
  
    })
    

    
    httpGet([uri:"https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=current,minutely,hourly&appid=$owmAPI&units=$unitsParsed"], { response ->
        
        // Rain Day 1
        
        rainDay1Poll = response.data.daily.rain[0]
        
        if(!rainDay1Poll) {
            rainDay1Poll = 0
        }
        updateDataValue("rainDay1", "$rainDay1Poll")
        sendEvent(name: "rainDay1", value: rainDay1Poll)
        
        // Rain Day 2
        
        rainDay2Poll = response.data.daily.rain[1]
        
        if(!rainDay2Poll) {
            rainDay2Poll = 0
        }
        updateDataValue("rainDay2", "$rainDay2Poll")
        sendEvent(name: "rainDay2", value: rainDay2Poll)
        
        // Rain Day 3
        
        rainDay3Poll = response.data.daily.rain[2]
        
        if(!rainDay3Poll) {
            rainDay3Poll = 0
        }
        updateDataValue("rainDay3", "$rainDay3Poll")
        sendEvent(name: "rainDay3", value: rainDay3Poll)
        
    })
    
}


// Polls Weather Environment Canada Alert Information

def ec() {

        httpGet([uri:"${rssFeed}"], { response ->
        
        alertPoll = response.data.entry.summary[0]
            if(logEnable) log.debug "Polled Weather Alert Information: $alertPoll"
            
            if(!alertPoll) {
                alertPoll = "Unavailable"
            }
  
    })
    
    updateDataValue("alert", "$alertPoll")
    sendEvent(name: "alert", value: alertPoll)
   
    
}
