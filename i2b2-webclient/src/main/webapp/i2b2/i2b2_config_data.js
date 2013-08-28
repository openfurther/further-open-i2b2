{
	urlProxy: "index.php",
	urlFramework: "js-i2b2/",
	//-------------------------------------------------------------------------------------------
	// THESE ARE ALL THE DOMAINS A USER CAN LOGIN TO
	lstDomains: [
		{ name: "localhost",
		  domain: "FURTHeR",
		  debug: true,
		  urlCellPM: "http://localhost:7070/axis2/rest/PMService/"
		},
		{ name: "i2b2.org (1.3)",
		  domain: "HarvardDemo",
		  debug: true,
		  urlCellPM: "http://services.i2b2.org/PM/rest/PMService/"
		},
		{ name: "i2b2.org (1.5)",
			  domain: "HarvardDemo15",
			  debug: true,
			  urlCellPM: "http://webservices.i2b2.org/i2b2/rest/PMService/"
		}
	]
	//-------------------------------------------------------------------------------------------
}
