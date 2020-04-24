/***********************************************************************************************************************
 *
 *  This is a Hubitat device driver which will connect to the Solaredge monitoring API and retrive inverter and
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

private version() { return "1.0.0" }

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

      attribute "production", "number"
    	attribute "consumption", "number"
      attribute "self_consumption", "number"
    	attribute "feed_in", "number"
    	attribute "purchased", "number"
      attribute "last_day", "number"
      attribute "last_month", "number"
      attribute "last_year", "number"
      attribute "lifetime", "number"
      attribute "energy_tile", "string"
      attribute "overview_tile", "string"
    }

    preferences {
	     section("Configure Solaredge API connection parameters") {
          input("site_id", "number", title: "Site Id", description: "Site Id to monitor.", required: true)
          input("api_key", "text", title: "API Key", description: "API key for given site id. This key is generated using installer level permissions. Ask installer to generate key or setup as sub-account with the correct permissions. Or sign up as an installer and take ownership of the inverter.", required: true)
          input("url", "text", title: "Solaredge Monitor API URL", description: "Endpoint URL for Solaredge Monitoring API.", required: true, defaultValue: "https://monitoringapi.solaredge.com")
          input("refresh_interval", "enum", title: "How often to refresh data", options: ["10", "15", "30"], required: true)
          input("energy_title", "text", title: "Daily Energy Details Title", description: "Title for energy details tile. Empty value removes title from tile.", required: false)
          input("overview_title", "text", title: "Production Overview Title", description: "Title for solar produtcion overview tile. Empty value removes title from tile.", required: false)
          input(name: "display_last_updated", type: "bool", title: "Display last updated timestamp?", defaultValue: false)
          input(name: "debug", type: "bool", title: "Enable debug logging", defaultValue: false)
      }
    }
}

def installed() {
	log.info "Solaredge Monitoring has been installed."

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
    sendEvent(name: "lifetime", value: 0)])

  state.version = version()
}

def uninstalled() {
	if (debug) log.info "Solaredge Monitoring has been uninstalled."

  unschedule(refresh)
}

def refresh() {

  if (debug) log.debug "Solaredge API is being queried."

  queryEnergyEndpoint()
  queryOverviewEndpoint()
  updateTiles()

  state.last_updated = new Date().format("YYYY-MM-dd HH:mm:ss")
}

def updated() {

  if (debug) {
      log.debug "Settings updated."
  }

  schedule("0 */${settings.refresh_interval} * ? * * *", refresh)
  updateTiles()

  state.version = version()
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

      sendEvent(name: "power", value: r.data.overview.currentPower.power)
      sendEvent(name: "last_day", value: r.data.overview.lastDayData.energy)
      sendEvent(name: "last_month", value: r.data.overview.lastMonthData.energy)
      sendEvent(name: "last_year", value: r.data.overview.lastYearData.energy)
      sendEvent(name: "lifetime", value: r.data.overview.lifeTimeData.energy)
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
}

def updateTiles() {

  if (debug) log.debug "Updating tile information."

  def production = device.currentValue("production")
  def production_status = "red"

  if (device.currentValue("production") < device.currentValue("consumption"))
  {
      production_status = "green"
  }

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

}

private formatEnergy(energy)
{
  if (energy < 1000) return energy + " Wh"

  if (energy < 1000000) return energy/1000 + " kWh"

  return energy/1000/1000 + " MWh"
}
