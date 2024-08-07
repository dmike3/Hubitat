/**
 *  _______  ________._.
 *  \      \ \_____  \ |
 *  /   |   \  _(__  < |
 * /    |    \/       \|
 * \____|__  /______  /_
 *        \/       \/\/
 *          - development
 *
 * Name: Weather Canada (OWM3.0-EC)
 * Version: 1.0.3
 * Author: n3!
 * 
 * Description: Polls weather information from OpenWeatherMap and Weather Environment Canada (Alert RSS Feed - https://weather.gc.ca/).
 *
 * Features: Current Weather and Canadian Weather Alerts
 *
 * Driver: https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Weather%20Canada%20(OWM-EC)/Weather%20Canada%20(OWM-EC).groovy
 *
 * Readme: https://raw.githubusercontent.com/dmike3/Hubitat/master/Drivers/Weather%20Canada%20(OWM-EC)/readme.txt
 *
 * Code: Referencing and pulling code from other awesome weather apps and suggestions. Our Hubitat community rocks!
 *
 *-------------------------------------------------------------------------------------------------------------------
 * Copyright 2020 n3! development
 * 
 * The following software is to be used "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. 
 *-------------------------------------------------------------------------------------------------------------------
 *
 * Change Log
 *
 * - Moved from API 2.5 to 3.0 - Remember to setup a subscription. 1000 requests free per day. (July 21, 2024)
 * - Fixed bug with weather icon (June 27, 2020)
 * - Included unit measurements for Weather Tile (June 27, 2020)
 *
 **/

import groovy.transform.Field

@Field static List timeOptions = [
    
    "Disabled",
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
    input name: "lat", type: "text",   title: "Latitude", required: true
    input name: "lon", type: "text",   title: "Longitude", required: true
    input name: "units", type: "enum", title: "Unit Setting", required: true, multiple: false, defaultValue: unitOptions[0], options: unitOptions
    input name: "pollTime", type: "enum", title: "Poll Time", required: true, multiple: false, defaultValue: timeOptions[3], options: timeOptions
    input name: "logEnable", type: "bool",   title: "Enable debug logging", defaultValue: false, required: true
}

metadata {
      
   definition (
   name: "Weather Canada (OWM-EC)",
   namespace: "n3!",
   author: "n3! development",
   importUrl: "https://github.com/dmike3/Hubitat/blob/master/Drivers/Weather%20OWM-EC%20Canada/weather-owm-ec-canada.groovy") {
        
   capability "Refresh"
   capability "Initialize"
   capability "Temperature Measurement"
   capability "RelativeHumidityMeasurement"
   capability "Pressure Measurement"

   attribute "alert", "string"
   attribute "alertSummary", "string"
   attribute "timezone", "string"
   attribute "weather", "string"
   attribute "feels_like", "number"
   attribute "windSpeed", "number"
   attribute "windDirection", "number"
   attribute "windGust", "number"
   attribute "visibility", "number"
   attribute "clouds", "number"
   attribute "country", "string"
   attribute "sunRise", "string"
   attribute "sunSet", "string"
   attribute "dewPoint", "number"
   attribute "rainToday", "number"
   attribute "rainTomorrow", "number"
   attribute "rainAfterTomorrow", "number"
   attribute "snowToday", "number"
   attribute "snowTomorrow", "number"
   attribute "snowAfterTomorrow", "number"
   attribute "tempToday", "number"
   attribute "tempToday_min", "number"
   attribute "tempToday_max", "number"
   attribute "dewPointToday", "number"
   attribute "moonriseToday", "number"
   attribute "moonsetToday", "number"
   attribute "dailySummary", "string"
   attribute "weatherTile", "string" 
   }
}

def initialize(){
   
    getWeather()
    
    if (pollTime == "Disabled") {
        unschedule(getWeather)   
    }
    
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
    unschedule(getWeather)   
    initialize()
    
}

def refresh() {
    unschedule(getWeather)
    initialize()
        
}

def poll() {
    unschedule(getWeather)
    initialize()
        
}

// Commands

    command "poll"

// Event Handlers

def getWeather() {
       
    // State Variables

    state.Version = '1.0.3'  
    
    // Parse Units

    if(units == "Celsius") {
        unitsParsed = "metric"
        tempUnit = "c"
    }
    else {
        unitsParsed = "imperial"
        tempUnit = "f"
    }
    
    if(logEnable) log.debug "Weather: Units are set to $unitsParsed"

    // Gets SunRise and SunSet Information from Hub
    def riseAndSet = getSunriseAndSunset()
    updateDataValue("sunRise", "$riseAndSet.sunrise")
    updateDataValue("sunSet", "$riseAndSet.sunset")
    sendEvent(name: "sunRise", value: riseAndSet.sunrise)
    sendEvent(name: "sunSet", value: riseAndSet.sunset)
    
    ec()
    ow()    
}

// Polls OpenWeatherMap API One Call 3.0

def ow() {
    
    log.info "Weather: Polling Weather"
    
    httpGet([uri:"https://api.openweathermap.org/data/3.0/onecall?lat=$lat&lon=$lon&appid=$owmAPI&units=$unitsParsed"], { response ->        
        
        // Timezone
        
        timezonePoll = response.data.timezone
        
        if(!timezonePoll) {
            timezonePoll = "Unavailable"
        }
                
        updateDataValue("timezone", "$timezonePoll")
        sendEvent(name: "timezone", value: timezonePoll)

        // Daily Summary
        
        dailysummaryPoll = response.data.daily.summary[0]
        
        if(!dailysummaryPoll) {
            dailysummaryPoll = 0
        }
        updateDataValue("dailysummary", "$dailysummaryPoll")
        sendEvent(name: "dailysummary", value: dailysummaryPoll)

        // Weather
        
        weatherPoll = response.data.current.weather.description
        
        if(!weatherPoll) {
            weatherPoll = "Unavailable"
        }
            
        updateDataValue("weather", "$weatherPoll")
        sendEvent(name: "weather", value: weatherPoll)
        
        // Temperature
                
        tempPoll = response.data.current.temp
        
        if(!tempPoll) {
            tempPoll = "Unavailable"
        }
        updateDataValue("temperature", "$tempPoll")
        sendEvent(name: "temperature", value: tempPoll)
        
        // Feels Like
        
        feelsLikePoll = response.data.current.feels_like
        
        if(!feelsLikePoll) {
             feelsLikePoll = 0
        }
        
        updateDataValue("feels_like", "$feelsLikePoll")
        sendEvent(name: "feels_like", value: feelsLikePoll)
        
        
        // Pressure
        
        pressurePoll = response.data.current.pressure
        
        if(!pressurePoll) {
             pressurePoll = 0   
        }
        
        updateDataValue("pressure", "$pressurePoll")
        sendEvent(name: "pressure", value: pressurePoll)
        
        // Humidity
        
        humidityPoll = response.data.current.humidity
        
        if(!humidityPoll) {
             humidityPoll = 0   
        }
          
        updateDataValue("humidity", "$humidityPoll")
        sendEvent(name: "humidity", value: humidityPoll)
        
        // Visibility
        
        visibilityPoll = response.data.current.visibility
        
        if(!visibilityPoll) {
             visibilityPoll = 0   
        }
        
        updateDataValue("visibility", "$visibilityPoll")
        sendEvent(name: "visibility", value: visibilityPoll)
        
        // Wind Speed
        
        windSpeedPoll = response.data.current.wind_speed
        if(!windSpeedPoll) {
            windSpeedPoll = 0   
        }
        
        updateDataValue("windSpeed", "$windSpeedPoll")
        sendEvent(name: "windSpeed", value: windSpeedPoll)
        
        // Wind Direction
        
        windDirectionPoll = response.data.current.wind_deg
        
        if(!windDirectionPoll) {
             windDirectionPoll = 0   
        }
        
        updateDataValue("windDirection", "$windDirectionPoll")
        sendEvent(name: "windDirection", value: windDirectionPoll)
        
        // Clouds
        
        cloudsPoll = response.data.current.clouds
        
        if(!cloudsPoll) {
             cloudsPoll = 0   
        }
        updateDataValue("clouds", "$cloudsPoll")
        sendEvent(name: "clouds", value: cloudsPoll)
        
        // Dewpoint Now
        
        dewPointPoll = response.data.current.dew_point
        
        if(!dewPointPoll) {
            dewPointPoll = 0
        }
        updateDataValue("dewPoint", "$dewPointPoll")
        sendEvent(name: "dewPoint", value: dewPointPoll)

        // Windgust Now
        
        dewPointPoll = response.data.current.wind_gust
        
        if(!wind_gustPoll) {
            wind_gustPoll = 0
        }
        updateDataValue("windGust", "$wind_gustPoll")
        sendEvent(name: "windGust", value: wind_gustPoll)
        
        // Rain Today
        
        rainTodayPoll = response.data.daily.rain[0]
        
        if(!rainTodayPoll) {
            rainTodayPoll = 0
        }
        updateDataValue("rainToday", "$rainTodayPoll")
        sendEvent(name: "rainToday", value: rainTodayPoll)
        
        // Rain Tomorrow
        
        rainTomorrowPoll = response.data.daily.rain[1]
        
        if(!rainTomorrowPoll) {
            rainTomorrowPoll = 0
        }
        updateDataValue("rainTomorrow", "$rainTomorrowPoll")
        sendEvent(name: "rainTomorrow", value: rainTomorrowPoll)
        
        // Rain AfterTomorrow
        
        rainAfterTomorrowPoll = response.data.daily.rain[2]
        
        if(!rainAfterTomorrowPoll) {
            rainAfterTomorrowPoll = 0
        }
        updateDataValue("rainAfterTomorrow", "$rainAfterTomorrowPoll")
        sendEvent(name: "rainAfterTomorrow", value: rainAfterTomorrowPoll)
        
        // Snow Today
        
        snowTodayPoll = response.data.daily.snow[0]
        
        if(!snowTodayPoll) {
            snowTodayPoll = 0
        }
        updateDataValue("snowToday", "$snowTodayPoll")
        sendEvent(name: "snowToday", value: snowTodayPoll)
        
        // Snow Tomorrow
        
        snowTomorrowPoll = response.data.daily.snow[0]
        
        if(!snowTomorrowPoll) {
            snowTomorrowPoll = 0
        }
        updateDataValue("snowTomorrow", "$snowTomorrowPoll")
        sendEvent(name: "snowTomorrow", value: snowTomorrowPoll)
        
        // Snow After Tomorrow
        
        snowAfterTomorrowPoll = response.data.daily.snow[0]
        
        if(!snowAfterTomorrowPoll) {
            snowAfterTomorrowPoll = 0
        }
        updateDataValue("snowAfterTomorrow", "$snowAfterTomorrowPoll")
        sendEvent(name: "snowAfterTomorrow", value: snowAfterTomorrowPoll)
        
        // Temp Today
        
        tempTodayPoll = response.data.daily.temp.day[0]
        
        if(!tempTodayPoll) {
            tempTodayPoll = 0
        }
        updateDataValue("tempToday", "$tempTodayPoll")
        sendEvent(name: "tempToday", value: tempTodayPoll)
        
    
       // Temp Today Min
        
        tempToday_minPoll = response.data.daily.temp.min[0]
        
        if(!tempToday_minPoll) {
            tempToday_minPoll = 0
        }
        updateDataValue("tempToday_min", "$tempToday_minPoll")
        sendEvent(name: "tempToday_min", value: tempToday_minPoll)
        
       // Temp Today Max
        
        tempToday_maxPoll = response.data.daily.temp.max[0]
        
        if(!tempToday_maxPoll) {
            tempToday_maxPoll = 0
        }
        updateDataValue("tempToday_max", "$tempToday_maxPoll")
        sendEvent(name: "tempToday_max", value: tempToday_maxPoll)
        
        // Dewpoint Daily
        
        dewPointTodayPoll = response.data.daily.dew_point[0]
        
        if(!dewPointTodayPoll) {
            dewPointTodayPoll = 0
        }
        updateDataValue("dewPointToday", "$dewPointTodayPoll")
        sendEvent(name: "dewPointToday", value: dewPointTodayPoll)

        // Moonrise Today
        
        moonriseTodayPoll = response.data.daily.moonrise[0]
        
        if(!moonriseTodayPoll) {
            moonriseTodayPoll = 0
        }
        updateDataValue("moonriseToday", "$moonriseTodayPoll")
        sendEvent(name: "moonriseToday", value: moonriseTodayPoll)


        // Moonset Today
        
        moonsetTodayPoll = response.data.daily.moonset[0]
        
        if(!moonsetTodayPoll) {
            moonsetTodayPoll = 0
        }
        updateDataValue("moonsetToday", "$moonsetTodayPoll")
        sendEvent(name: "moonsetToday", value: moonsetTodayPoll)
        
    })
    
    if(logEnable) log.debug "Weather: Polling Weather Icon"
    
    // Get Weather Icon  http://openweathermap.org/img/wn/01d@2x.png
    
        httpGet([uri:"https://api.openweathermap.org/data/3.0/onecall?lat=$lat&lon=$lon&appid=$owmAPI&units=$unitsParsed"], { response ->
        //httpGet([uri:"http://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$owmAPI&units=$unitsParsed"], { response ->
            
            //condition_iconPoll = response.data.weather.icon.toString().minus('[').minus(']')
            condition_iconPoll = response.data.current.weather.icon[0].toString()
            
            conditionURL = "http://openweathermap.org/img/wn/$condition_iconPoll@2x.png"                   
        })

    // End of Weather Icon
    
    // Weather Tile - Used for Dashboard
    
    def tiletxt = '<div style=\"text-align:center;display:inline;font-size:0.65em;line-height=65%;margin-top:0em;margin-bottom:0em;\"><b>' + "${timezonePoll}" + '</b></div><br> ' + ""
    tiletxt+='<div style=\"text-align:center;display:inline;font-size:1em;line-height=100%;margin-top:0em;margin-bottom:0em;\">' + "${weatherPoll}" + "<br>"  
    tiletxt+="<img src='$conditionURL' width='50' height='50' /><br>"
    tiletxt+="${tempPoll}" + " $tempUnit" + '<span style = \"font-size:.65em;\"> Feels like ' + "${feelsLikePoll}" + " $tempUnit" + '</span><br>'
    tiletxt+='<div style=\"text-align:center;font-size:.65em;line-height=50%;margin-top:0em;margin-bottom:0em;\"><b>Wind Speed:</b>' + " ${windSpeedPoll}" +  ' <b>Humidity:</b>' + " ${humidityPoll} %" + ' <b>Rain Today:</b>' + " ${rainTodayPoll}" + '<br></div>'
	sendEvent(name: "weatherTile", value: tiletxt, displayed: true)       
}

// Polls Weather Environment Canada Alert Information

def ec() {
    
        if(logEnable) log.debug "Weather: Polling WNC Alerts"

        httpGet([uri:"${rssFeed}"], { response ->
        
        alertPoll = response.data.entry.title[0]
        alertSummaryPoll = response.data.entry.summary[0]   
                             
            if(!alertPoll) {
                alertPoll = "Unavailable"
            }
            
            if(!alertSummaryPoll) {
                alertSummaryPoll = "Unavailable"
            }
            
            sendEvent(name: "alert", value: alertPoll)
            sendEvent(name: "alertSummary", value: alertSummaryPoll)
            updateDataValue("alert", "$alertPoll")
            updateDataValue("alertSummary", "$alertSummaryPoll")
            if(logEnable) log.debug "Weather: Alert $alertPoll"
            if(logEnable) log.debug "Weather: Alert Summary $alertSummaryPoll"
    })    
}
