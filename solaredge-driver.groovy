/***********************************************************************************************************************
 *
 *  This is a Hubitat device driver which will connect to the Solaredge monitoring API and retrieve inverter and
 *  site overview data.
 *
 *  License:
 *  This program falls under the GNU GENERAL PUBLIC LICENSE Version 3.
 *  For full license visit https://github.com/funzie19/hubitat-solaredge/blob/master/LICENSE
 *
 *  Name: Solaredge Monitoring Driver
 *
 *  Changelog: https://github.com/funzie19/hubitat-solaredge/blob/master/CHANGELOG.md
 *
 ***********************************************************************************************************************/

private version() { return "1.1.2" }

metadata {
    definition(
        name: "Solaredge Monitoring Driver",
        namespace: "funzie19.solaredge",
        author: "Joseph Kregloh",
        importUrl: "https://raw.githubusercontent.com/funzie19/hubitat-solaredge/master/solaredge-driver.groovy",
        description: "Driver to query the Solaredge API endpoint and return data related to solar production and consumption for a given site."
    ) {
      capability "Power Meter"
      capability "Refresh"
      command "updateTiles"
      command "getEnergy"
      command "getOverview"
      command "getSitePowerFlow"
      command "clearData"

      attribute "production", "number"
    	attribute "consumption", "number"
      attribute "self_consumption", "number"
    	attribute "feed_in", "number"
    	attribute "purchased", "number"
      attribute "last_day", "number"
      attribute "last_month", "number"
      attribute "last_year", "number"
      attribute "lifetime", "number"
      attribute "grid_power", "string"
      attribute "load_power", "string"
      attribute "pv_power", "string"
      attribute "energy_tile", "string"
      attribute "overview_tile", "string"
      attribute "power_flow_tile", "string"
    }

    preferences {
	     section("Configure Solaredge API connection parameters") {
          input("site_id", "number", title: "Site Id", description: "Site Id to monitor.", required: true)
          input("api_key", "text", title: "API Key", description: "API key for given site id. This key is generated using installer level permissions. Ask installer to generate key or setup as sub-account with the correct permissions. Or sign up as an installer and take ownership of the inverter.", required: true)
          input("url", "text", title: "Solaredge Monitor API URL", description: "Endpoint URL for Solaredge Monitoring API.", required: true, defaultValue: "https://monitoringapi.solaredge.com")

          input("energy_refresh_interval", "enum", title: "How often to refresh the energy data", options: [
            255:"Do NOT update",
            1:"1 Minute",
            2:"2 Minutes",
            3:"3 Minutes",
            4:"4 Minutes",
            5:"5 Minutes",
            6:"6 Minutes",
            7:"7 Minutes",
            8:"8 Minutes",
            9:"9 Minutes",
            10:"10 Minutes",
            11:"11 Minutes",
            12:"12 Minutes",
            13:"13 Minutes",
            14:"14 Minutes",
            15:"15 Minutes",
            16:"16 Minutes",
            17:"17 Minutes",
            18:"18 Minutes",
            19:"19 Minutes",
            20:"20 Minutes",
            21:"21 Minutes",
            22:"22 Minutes",
            23:"23 Minutes",
            24:"24 Minutes",
            25:"25 Minutes",
            26:"26 Minutes",
            27:"27 Minutes",
            28:"28 Minutes",
            29:"29 Minutes",
            30:"30 Minutes",
            31:"31 Minutes",
            32:"32 Minutes",
            33:"33 Minutes",
            34:"34 Minutes",
            35:"35 Minutes",
            36:"36 Minutes",
            37:"37 Minutes",
            38:"38 Minutes",
            39:"39 Minutes",
            40:"40 Minutes",
            41:"41 Minutes",
            42:"42 Minutes",
            43:"43 Minutes",
            44:"44 Minutes",
            45:"45 Minutes",
            46:"46 Minutes",
            47:"47 Minutes",
            48:"48 Minutes",
            49:"49 Minutes",
            50:"50 Minutes",
            51:"51 Minutes",
            52:"52 Minutes",
            53:"53 Minutes",
            54:"54 Minutes",
            55:"55 Minutes",
            56:"56 Minutes",
            57:"57 Minutes",
            58:"58 Minutes",
            59:"59 Minutes"
          ], required: true)
          input("overview_refresh_interval", "enum", title: "How often to refresh the production overview data", options: [
            255:"Do NOT update",
            1:"1 Minute",
            2:"2 Minutes",
            3:"3 Minutes",
            4:"4 Minutes",
            5:"5 Minutes",
            6:"6 Minutes",
            7:"7 Minutes",
            8:"8 Minutes",
            9:"9 Minutes",
            10:"10 Minutes",
            11:"11 Minutes",
            12:"12 Minutes",
            13:"13 Minutes",
            14:"14 Minutes",
            15:"15 Minutes",
            16:"16 Minutes",
            17:"17 Minutes",
            18:"18 Minutes",
            19:"19 Minutes",
            20:"20 Minutes",
            21:"21 Minutes",
            22:"22 Minutes",
            23:"23 Minutes",
            24:"24 Minutes",
            25:"25 Minutes",
            26:"26 Minutes",
            27:"27 Minutes",
            28:"28 Minutes",
            29:"29 Minutes",
            30:"30 Minutes",
            31:"31 Minutes",
            32:"32 Minutes",
            33:"33 Minutes",
            34:"34 Minutes",
            35:"35 Minutes",
            36:"36 Minutes",
            37:"37 Minutes",
            38:"38 Minutes",
            39:"39 Minutes",
            40:"40 Minutes",
            41:"41 Minutes",
            42:"42 Minutes",
            43:"43 Minutes",
            44:"44 Minutes",
            45:"45 Minutes",
            46:"46 Minutes",
            47:"47 Minutes",
            48:"48 Minutes",
            49:"49 Minutes",
            50:"50 Minutes",
            51:"51 Minutes",
            52:"52 Minutes",
            53:"53 Minutes",
            54:"54 Minutes",
            55:"55 Minutes",
            56:"56 Minutes",
            57:"57 Minutes",
            58:"58 Minutes",
            59:"59 Minutes"
          ], required: true)
          input("power_flow_refresh_interval", "enum", title: "How often to refresh power flow data", options: [
            255:"Do NOT update",
            1:"1 Minute",
            2:"2 Minutes",
            3:"3 Minutes",
            4:"4 Minutes",
            5:"5 Minutes",
            6:"6 Minutes",
            7:"7 Minutes",
            8:"8 Minutes",
            9:"9 Minutes",
            10:"10 Minutes",
            11:"11 Minutes",
            12:"12 Minutes",
            13:"13 Minutes",
            14:"14 Minutes",
            15:"15 Minutes",
            16:"16 Minutes",
            17:"17 Minutes",
            18:"18 Minutes",
            19:"19 Minutes",
            20:"20 Minutes",
            21:"21 Minutes",
            22:"22 Minutes",
            23:"23 Minutes",
            24:"24 Minutes",
            25:"25 Minutes",
            26:"26 Minutes",
            27:"27 Minutes",
            28:"28 Minutes",
            29:"29 Minutes",
            30:"30 Minutes",
            31:"31 Minutes",
            32:"32 Minutes",
            33:"33 Minutes",
            34:"34 Minutes",
            35:"35 Minutes",
            36:"36 Minutes",
            37:"37 Minutes",
            38:"38 Minutes",
            39:"39 Minutes",
            40:"40 Minutes",
            41:"41 Minutes",
            42:"42 Minutes",
            43:"43 Minutes",
            44:"44 Minutes",
            45:"45 Minutes",
            46:"46 Minutes",
            47:"47 Minutes",
            48:"48 Minutes",
            49:"49 Minutes",
            50:"50 Minutes",
            51:"51 Minutes",
            52:"52 Minutes",
            53:"53 Minutes",
            54:"54 Minutes",
            55:"55 Minutes",
            56:"56 Minutes",
            57:"57 Minutes",
            58:"58 Minutes",
            59:"59 Minutes"
          ], required: true)
          input("energy_title", "text", title: "Daily Energy Details Title", description: "Title for energy details tile. Empty value removes title from tile.", required: false)
          input("overview_title", "text", title: "Production Overview Title", description: "Title for solar produtcion overview tile. Empty value removes title from tile.", required: false)
          input("power_flow_title", "text", title: "Power Flow Title", description: "Title for current flow overview(PV, load, and grid). Empty value removes title from tile.", required: false)
          input(name: "display_last_updated", type: "bool", title: "Display last updated timestamp?", defaultValue: false)
          input(name: "debug", type: "bool", title: "Enable debug logging", defaultValue: false)
      }
    }
}

def installed() {
	log.info "Solaredge Monitoring has been installed."

  clearData()

  state.version = version()
}

def uninstalled() {
	if (debug) log.info "Solaredge Monitoring has been uninstalled."

  unschedule(refresh)
  unschedule(queryEnergyEndpoint)
  unschedule(queryOverviewEndpoint)
  unschedule(querySitePowerFlowEndpoint)
}

def refresh() {

  if (debug) log.debug "Solaredge API is being queried."

  queryEnergyEndpoint()
  queryOverviewEndpoint()
  querySitePowerFlowEndpoint()

  updateTiles()
}

def updated() {

  if (debug) {
      log.debug "Settings updated."
  }

  unschedule(refresh)
  unschedule(queryEnergyEndpoint)
  unschedule(queryOverviewEndpoint)
  unschedule(querySitePowerFlowEndpoint)

  if( settings.energy_refresh_interval != "255") schedule("0 */${settings.energy_refresh_interval} * ? * * *", queryEnergyEndpoint)
  if( settings.overview_refresh_interval != "255") schedule("0 */${settings.overview_refresh_interval} * ? * * *", queryOverviewEndpoint)
  if( settings.power_flow_refresh_interval != "255") schedule("0 */${settings.power_flow_refresh_interval} * ? * * *", querySitePowerFlowEndpoint)

  updateTiles()

  state.version = version()
}

def getEnergy() {
  queryEnergyEndpoint()
  updateTiles()
}

def getOverview() {
  queryOverviewEndpoint()
  updateTiles()
}

def getSitePowerFlow() {
  querySitePowerFlowEndpoint()
  updateTiles()
}

def clearData()
{
  if (debug) log.debug "Deleting all metrics."

  delayBetween([
    sendEvent(name: "feed_in", value: 0),
    sendEvent(name: "self_consumption", value: 0),
    sendEvent(name: "purchased", value: 0),
    sendEvent(name: "consumption", value: 0),
    sendEvent(name: "production", value: 0),
    sendEvent(name: "power", value: 0),
    sendEvent(name: "last_day", value: 0),
    sendEvent(name: "last_month", value: 0),
    sendEvent(name: "last_year", value: 0),
    sendEvent(name: "grid_power", value: "0 kW"),
    sendEvent(name: "load_power", value: "0 kW"),
    sendEvent(name: "pv_power", value: "0 kW"),
    sendEvent(name: "energy_tile", value: ""),
    sendEvent(name: "overview_tile", value: ""),
    sendEvent(name: "power_flow_tile", value: ""),
    sendEvent(name: "lifetime", value: 0)])

    updateTiles()
}

def queryEnergyEndpoint() {

  if (debug) log.debug "Gathering energy metrics."

  def today =  timeToday("00:00").format("YYYY-MM-dd")

  def params = [
    uri: "${settings.url}",
    path: "/site/${settings.site_id}/energyDetails",
    query: [timeUnit: "DAY", startTime: "${today} 00:00:00", endTime: "${today} 23:59:59", api_key: "${settings.api_key}"]
  ]

  try {
    httpGet(params) { r ->

      def meters = r.data.energyDetails.meters

      if (debug) {
        log.debug "Status: ${r.getStatus()}"
        log.debug "Headers: ${r.getAllHeaders()}"
        log.debug "Content Type: ${r.getContentType()}"
        log.debug "Response: ${r.data}"
        log.debug "meters: ${meters}"
      }

      meters.each { meter ->
        switch(meter.type) {
          case "FeedIn":
            if (meter.values[0].value == null) meter.values[0].value = 0.0
            sendEvent(name: "feed_in", value: meter.values[0].value)
            if (debug) log.debug "feed_in: ${meter.values[0].value}"
            break
          case "SelfConsumption":
            if (meter.values[0].value == null) meter.values[0].value = 0.0
            sendEvent(name: "self_consumption", value: meter.values[0].value)
            if (debug) log.debug "self_consumption: ${meter.values[0].value}"
            break
          case "Purchased":
            if (meter.values[0].value == null) meter.values[0].value = 0.0
            sendEvent(name: "purchased", value: meter.values[0].value)
            if (debug) log.debug "purchased: ${meter.values[0].value}"
            break
          case "Consumption":
            if (meter.values[0].value == null) meter.values[0].value = 0.0
            sendEvent(name: "consumption", value: meter.values[0].value)
            if (debug) log.debug "consumption: ${meter.values[0].value}"
            break
          case "Production":
            if (meter.values[0].value == null) meter.values[0].value = 0.0
            sendEvent(name: "production", value: meter.values[0].value)
            if (debug) log.debug "production: ${meter.values[0].value}"
            break
        }
      }
    }
  } catch (Exception e) {
    log.error "Exception"

    if(e.getStatusCode()) {
      switch(e.getStatusCode()) {
          case 401:
              log.error "401 - Not authorized"
              break
          case 403:
              log.error "403 - Forbidden"
              break
          case 429:
              log.error "429 - Too many requests"
              break
          default:
              log.error "Unkown Error"
              log.error "httpGet status code: e.getStatusCode ${e.getStatusCode()}"
              log.error "httpGet message: e.message : ${e.message}"
              log.error "httpGet full stack: e : ${e}"
              break
      }
    }
  }
    
    updateTiles()
}

def queryOverviewEndpoint() {

  if (debug) log.debug "Gathering overview metrics."

  def params = [
    uri: "${settings.url}",
    path: "/site/${settings.site_id}/overview",
    query: [api_key: "${settings.api_key}"]
  ]

  try {
    httpGet(params) { r ->

      if (debug) {
        log.debug "Status: ${r.getStatus()}"
        log.debug "Headers: ${r.getAllHeaders()}"
        log.debug "Content Type: ${r.getContentType()}"
        log.debug "Response: ${r.data}"
      }

      delayBetween([
        sendEvent(name: "power", value: r.data.overview.currentPower.power),
        sendEvent(name: "last_day", value: r.data.overview.lastDayData.energy),
        sendEvent(name: "last_month", value: r.data.overview.lastMonthData.energy),
        sendEvent(name: "last_year", value: r.data.overview.lastYearData.energy),
        sendEvent(name: "lifetime", value: r.data.overview.lifeTimeData.energy)
      ])
    }
  } catch (Exception e) {
      log.error "Exception"

      if(e.getStatusCode()) {
        switch(e.getStatusCode()) {
            case 401:
                log.error "401 - Not authorized"
                break
            case 403:
                log.error "403 - Forbidden"
                break
            case 429:
                log.error "429 - Too many requests"
                break
            default:
                log.error "Unkown Error"
                log.error "httpGet status code: e.getStatusCode ${e.getStatusCode()}"
                log.error "httpGet message: e.message : ${e.message}"
                log.error "httpGet full stack: e : ${e}"
                break
      }
    }
  }
    
    updateTiles()
}

def querySitePowerFlowEndpoint() {

  if (debug) log.debug "Gathering site power flow data."

  def params = [
    uri: "${settings.url}",
    path: "/site/${settings.site_id}/currentPowerFlow",
    query: [api_key: "${settings.api_key}"]
  ]

  try {
    httpGet(params) { r ->

      if (debug) {
        log.debug "Status: ${r.getStatus()}"
        log.debug "Headers: ${r.getAllHeaders()}"
        log.debug "Content Type: ${r.getContentType()}"
        log.debug "Response: ${r.data}"
      }

      r.data.siteCurrentPowerFlow.PV.currentPower = 0
      r.data.siteCurrentPowerFlow.LOAD.currentPower = 1
        r.data.siteCurrentPowerFlow.GRID.currentPower = 0
        
      delayBetween([
        sendEvent(name: "grid_power", value: r.data.siteCurrentPowerFlow.GRID.currentPower + " " + r.data.siteCurrentPowerFlow.unit),
        sendEvent(name: "load_power", value: r.data.siteCurrentPowerFlow.LOAD.currentPower + " " + r.data.siteCurrentPowerFlow.unit),
        sendEvent(name: "pv_power", value: r.data.siteCurrentPowerFlow.PV.currentPower + " " + r.data.siteCurrentPowerFlow.unit)
      ])

      
        
      if (r.data.siteCurrentPowerFlow.PV.currentPower - r.data.siteCurrentPowerFlow.LOAD.currentPower >= 0) {
          state.flow_direction = "green"
      }
      else {
        state.flow_direction = "red"
      }
    }
  } catch (Exception e) {
      log.error "Exception"

      if(e.getStatusCode()) {
        switch(e.getStatusCode()) {
            case 401:
                log.error "401 - Not authorized"
                break
            case 403:
                log.error "403 - Forbidden"
                break
            case 429:
                log.error "429 - Too many requests"
                break
            default:
                log.error "Unkown Error"
                log.error "httpGet status code: e.getStatusCode ${e.getStatusCode()}"
                log.error "httpGet message: e.message : ${e.message}"
                log.error "httpGet full stack: e : ${e}"
                break
      }
    }
  }
    
    updateTiles()
}

def updateTiles() {

  if (debug) log.debug "Updating tile information."
    
  state.last_updated = new Date().format("YYYY-MM-dd HH:mm:ss")

  def production = device.currentValue("production")
  def usage_color = "red"

  def energy_tile = "<div style='font-size: 13px;'><table width='100%'>"
  if (settings.energy_title) energy_tile += "<tr><td style='text-align: center; width: 100%'>" + settings.energy_title + "</td></tr>"
  energy_tile += "<tr><td style='text-align: left; width: 100%'>" + "Production: <span style='color: green;'>" + formatEnergy(device.currentValue("production")) + "</span></td></tr>"
  energy_tile += "<tr><td style='text-align: left; width: 100%'>" + "Consumption: "

  if (device.currentValue("production") < device.currentValue("consumption"))
  {
    energy_tile += "<span style='color: red;'>"
  }
  else
  {
    energy_tile += "<span style='color: green;'>"
  }

  energy_tile += formatEnergy(device.currentValue("consumption")) + "</span></td></tr>"
  energy_tile += "<tr><td style='text-align: left; width: 100%'>" + "Self Consumed: <span style='color: blue;'>" + formatEnergy(device.currentValue("self_consumption")) + "</span></td></tr>"
  energy_tile += "<tr><td style='text-align: left; width: 100%'>" + "Import: "

  if (device.currentValue("feed_in") < device.currentValue("purchased"))
  {
    energy_tile += "<span style='color: red;'>"
  }
  else
  {
    energy_tile += "<span style='color: green;'>"
  }

  energy_tile += formatEnergy(device.currentValue("purchased")) + "</td></tr>"
  energy_tile += "<tr><td style='text-align: left; width: 100%'>" + "Exported: <span style='color: green;'>" + formatEnergy(device.currentValue("feed_in")) + "</span></td></tr>"
  if (settings.display_last_updated == true) energy_tile += "<tr><td style='text-align: center; width: 100%'><br />Last Updated: " + state.last_updated + "</td></tr>"
  energy_tile += "</table></div>"

  sendEvent(name: "energy_tile", value: energy_tile)

  def overview_tile = "<div style='font-size: 15px;'><table width='100%'>"
  if (settings.overview_title) overview_tile += "<tr><td style='text-align: center; width: 100%'>" + settings.overview_title + "</td></tr>"
  overview_tile += "<tr><td style='text-align: left; width: 100%'>" + "Today: <span style='color: green;'>" + formatEnergy(device.currentValue("last_day")) + "</span></td></tr>"
  overview_tile += "<tr><td style='text-align: left; width: 100%'>" + "Monthly <span style='color: green;'>" + formatEnergy(device.currentValue("last_month")) + "</span></td></tr>"
  overview_tile += "<tr><td style='text-align: left; width: 100%'>" + "Yearly <span style='color: green;'>" + formatEnergy(device.currentValue("last_year")) + "</span></td></tr>"
  overview_tile += "<tr><td style='text-align: left; width: 100%'>" + "Lifetime <span style='color: green;'>" + formatEnergy(device.currentValue("lifetime")) + "</span></td></tr>"
  if (settings.display_last_updated == true) overview_tile += "<tr><td style='text-align: center; width: 100%'><br />Last Updated: " + state.last_updated + "</td></tr>"
  overview_tile += "</table></div>"

  sendEvent(name: "overview_tile", value: overview_tile)

  def power_flow_tile = "<div style='font-size: 15px;'><table width='100%'>"
  if (settings.power_flow_title) power_flow_tile += "<tr><td style='text-align: center; width: 100%'>" + settings.power_flow_title + "</td></tr>"
  if (device.currentValue("pv_power")) power_flow_tile += "<tr><td style='text-align: left; width: 100%'>" + "Solar: <span style='color: green;'>" + device.currentValue("pv_power") + "</span></td></tr>"
  if (device.currentValue("grid_power")) power_flow_tile += "<tr><td style='text-align: left; width: 100%'>" + "Grid: <span style='color: " + state.flow_direction + ";'>" + device.currentValue("grid_power") + "</span></td></tr>"

  if (device.currentValue("grid_power") == 0 || device.currentValue("load_power") > device.currentValue("grid_power"))
  {
    usage_color = "orange"
  }

  if (device.currentValue("load_power")) power_flow_tile += "<tr><td style='text-align: left; width: 100%'>" + "Usage: <span style='color: " + usage_color + ";'>" + device.currentValue("load_power") + "</span></td></tr>"

  if (settings.display_last_updated == true) power_flow_tile += "<tr><td style='text-align: center; width: 100%'><br />Last Updated: " + state.last_updated + "</td></tr>"
  power_flow_tile += "</table></div>"

  sendEvent(name: "power_flow_tile", value: power_flow_tile)
}

private formatEnergy(energy)
{
  if (energy < 1000) return energy + " Wh"

  if (energy < 1000000) return energy/1000 + " kWh"

  return energy/1000/1000 + " MWh"
}
