// this file contains a list of all files that need to be loaded dynamically for this i2b2 Cell
// every file in this list will be loaded after the cell's Init function is called
{
	files:[
		"IRBApplication_ctrlr.js"
	],
	css:[ 
		"vwIRBApplication.css"
	],
	config: {
		// additional configuration variables that are set by the system
		short_name: "Initiate an IRB Application",
		name: "Initiate an IRB Application",
		description: "This plugin allows user to initiate an IRB Application based on a query",
		category: ["celless","plugin","standard","irb-application"],
		plugin: {
			isolateHtml: false,  // this means do not use an IFRAME
			isolateComm: false,  // this means to expect the plugin to use AJAX communications provided by the framework
			standardTabs: true,  // this means the plugin uses standard tabs at top
			html: {
				source: 'injected_screens.html',
				mainDivId: 'IRBApplication-mainDiv'
			}
		}
	}
}