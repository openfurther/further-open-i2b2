/**
 * @projectDescription	Initialize the i2b2 framework & load the hive configuration information for the core I2B2 Framework.
 * @inherits 	i2b2
 * @namespace		i2b2
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */


// build the global i2b2.hive namespace
var i2b2 = {sdx:{TypeControllers:{},Master:{_sysData:{}}},events:{},hive:{cfg:{},helpers:{},base_classes:{}},h:{}};
if (undefined==i2b2) { var i2b2 = new Object; }
if (undefined==i2b2.sdx) { i2b2.sdx = new Object; }	
if (undefined==i2b2.events) { i2b2.events = new Object; }	
if (undefined==i2b2.hive) { i2b2.hive = new Object; }	
if (undefined==i2b2.hive.cfg) { i2b2.hive.cfg = new Object; }	
if (undefined==i2b2.h) { i2b2.h = new Object; }
if (undefined==i2b2.hive.base_classes) { i2b2.hive.base_classes = new Object; }

//     ||
//     ||		
//   \\||//		Configure the loading of cells BELOW
//    \\//
//     vv
// ================================================================================================== //
// THESE ARE ALL THE CELLS THAT ARE INSTALLED ONTO THE SERVER
i2b2.hive.tempCellsList = [
		{ 
			code: 	"PM",
			forceLoading: true 			// <----- this must be set to true for the PM cell!
		},
		{ 
			code: "ONT"	
		},
		{ 
			code: "CRC"	
		},
		{ 
			code: "WORK"	
		},
		{ 
			code: "FUR"	
		},
		{ 
			code:	"PLUGINMGR",
			forceLoading: true,
			forceConfigMsg: { params: [] }
		},
		{ 
			code:	"Dem1Set",
			forceLoading: true,
			forceConfigMsg: { params: [] },
			forceDir: "cells/plugins/standard"
		}

	];
// ================================================================================================== //
//     ^^
//    //\\
//   //||\\		
//     ||		Configure the loading of cells ABOVE
//     ||		
//     ||
















// ================================================================================================== //
i2b2.Init = function() {
	//load the (user configured) i2b2Hive configuration via JSON config file
	var config_data = i2b2.h.getJsonConfig('i2b2_config_data.js');
	if (!config_data) {
		alert("The user-defined I2B2 Hive Configuration message is invalid");
		return false;
	} else {
		i2b2.hive.cfg = config_data;
		i2b2.hive.cfg.lstCells = i2b2.hive.tempCellsList;
		delete tempCellsList;
	}
	
	// load the rest of the i2b2 framework files
	var config_data = i2b2.h.getJsonConfig(i2b2.hive.cfg.urlFramework+'hive/hive_config_data.js');
	if (!config_data) {
		alert("The I2B2 Hive Components Load message is invalid");
		return false;
	} else {
		var successHandler = function(oData) { 
			//code to execute when all requested scripts have been 
			//loaded; this code can make use of the contents of those 
			//scripts, whether it's functional code or JSON data. 
			console.info("EVENT FIRED i2b2.events.afterFrameworkInit");
			i2b2.events.afterFrameworkInit.fire();
			// Loading the hive cells
			var cl = i2b2.hive.cfg.lstCells;
			for (var i=0; i < cl.length; i++) {
				i2b2[cl[i].code] = new i2b2_BaseCell(cl[i]);
			}
			// we must always fully initialize the PM cell
			if (i2b2['PM']) { 
				// the project manager cell must fire the afterProjMngtInit event signal
				i2b2['PM'].Init();
			};			
			// trigger user events after everything is loaded
			console.info("EVENT FIRED i2b2.events.afterHiveInit");
			i2b2.events.afterHiveInit.fire();
		};
		var failureHandler = function(oData) {
			alert('i2b2 Framework file failed to load!\n'+oData);
		};
		
		var fl = [];
		for (var i=0; i<config_data.files.length; i++) {
			fl.push(i2b2.hive.cfg.urlFramework+'hive/'+config_data.files[i]+'?v=4');
		}
		
		YAHOO.util.Get.script(fl, { 
				onSuccess: successHandler, 
				onFailure: failureHandler,
				data:      config_data
		}); 
	}
}


// create our custom events
// ================================================================================================== //
i2b2.events.afterFrameworkInit = new YAHOO.util.CustomEvent("afterInit", i2b2);
i2b2.events._privLoadCells = new YAHOO.util.CustomEvent("priv_doLoadCells", i2b2);
i2b2.events._privRemoveInitFuncs = new YAHOO.util.CustomEvent("priv_doRemoveInit", i2b2);
i2b2.events.afterHiveInit = new YAHOO.util.CustomEvent("afterInit", i2b2);
i2b2.events.afterCellInit = new YAHOO.util.CustomEvent("afterInit", i2b2);
i2b2.events.afterLogin = new YAHOO.util.CustomEvent("afterLogin", i2b2);
i2b2.events.afterAllCellsLoaded = new YAHOO.util.CustomEvent("afterAllCellsLoaded", i2b2);


i2b2.events.afterAllCellsLoaded.subscribe((function(type,args) {
	// all cells have been loaded.  Set the viewMode, resize, etc
	alert('all cells are loaded');
}));


// *******************************************************
//  i2b2.h.getJsonConfig
//    
//    @descript This function retreves a JSON-defined configuration object from the given URL
// *******************************************************
i2b2.h.getJsonConfig = function(url) {
	var json = new Ajax.Request(url, {
		contentType: 'text/xml',
		method:'get', 
		asynchronous:false, 
		sanitizeJSON:true
	});
	try {
		var co = eval('('+json.transport.responseText+')');
	} catch(e) {
		var co = false;
	}
	return co;

}
