/**
 *  Emon CMS Logger
 *
 *  Copyright 2016 Dallan Wagner
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * Full credit to Brian Wilson (bdwilson) for most of the pushReading method. This was very closely mirrored from his "Plotwatt Logger" app.
 *
 */
definition(
    name: "Emon CMS Logger",
    namespace: "dallanwagz",
    author: "Dallan Wagner",
    description: "This device will take the current power utilization from an Aeon labs energy monitor and send it to Emon CMS via RESTful API.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {

	//ask user which power monitor to use in this Smart App
	section("Power Monitor") {
		input "powerMonitor", "capability.powerMeter", required: true, title: "Which monitor?"
    }
    
    //ask user to provide their API key for Emon CMS service
    section("Emon Write API Key"){
    	input "apiKey", "text", required: true, title: "What is your Emon CMS API key?"
   }
      
}

//default method called when app is installed, we aren't doing anything here
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

//default method called when app is updated, we aren't doing anything here either
def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

//this is where we delcare what events we are interested in from the "thing"
def initialize() {
	//here we are subscribing the ANY event that the power monitor throws
	subscribe(powerMonitor, "power", handlePowerEvent)
}

//this is where we "do stuff" after the monitor throws an event. in our case we are logging
//the given value and sending it to another method
def handlePowerEvent(evt) {
	//log.debug "$evt.power"
    pushReading(evt,"power") { it.toString() }
}

//here we are crafting the specific HTTP request to send to Emon CMS given the API key
//provided earlier and the reading that the power monitor sent to the event handler.

private pushReading(evt, field, Closure c) {
    def value = c(evt.value)
    float watts = value.toFloat()
    def kwatts = watts/1000
    def now = Calendar.instance
    def date = now.time
    def millis = date.time
    def secs = millis/1000
    secs = secs.toInteger()
        
    def params = [
         uri: "https://emoncms.org/input/post.json?node=0&csv=${evt.value}&apikey=${apiKey}"
    ] 
    
    log.debug "params = ${params}"
        
    try {
    	log.debug "running commmand: httpGet(${params})"
        httpGet(params)
	} catch (e) {
    	log.info "httpPost response: ${e}"
    }
    
}