/**
 *  _______  ________._.
 *  \      \ \_____  \ |
 *  /   |   \  _(__  < |
 * /    |    \/       \|
 * \____|__  /______  /_
 *        \/       \/\/
 *          - development
 *
 * Name: Xbox One Smartglass
 * Version: 1.0.3
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
 * June 24, 20202 - Updated timeout to not throw an error in the logs if it can't reach the specified xbox
 * June 12, 2020 - Added a front end for the driver. Parent/Child apps.
 * June 8, 2020 - Updated the default poll time. Included an App installer
 *
 **/

definition(
    name: "Xbox One Smartglass",
    namespace: "n3!",
    author: "n3! development",
    description: "Xbox One Smartglass",
    category: "Integration",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",)

state.version = "1.0.3"

preferences {
    page(name: "mainPage", title: "", install: true, uninstall: true,submitOnChange: true) {
        section {
            paragraph "<h1 style='color:green;font-weight: bold'><img src='https://github.com/dmike3/Hubitat/blob/master/Apps/Xbox-Smartglass/_graphics/xbox.png?raw=true'> Xbox One Smartglass</h1>"
            app(name: "childApps", appName: "Xbox One Smartglass Child", namespace: "n3!", title: "Create Xbox One Smartglass Child", multiple: true)
            }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    childApps.each {child ->
        log.debug "child app: ${child.label}"
    }
}
