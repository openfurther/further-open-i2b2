/**
 * @projectDescription	Controller object for Project Management.
 * @inherits 	i2b2
 * @namespace	i2b2.PM
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: cells > PM > ctrlr');
console.time('execute time');

// ================================================================================================== //
i2b2.PM.doLogin = function() {
	i2b2.PM.model.shrine_domain = false;
	// change the cursor
	// show on GUI that work is being done
	i2b2.h.LoadingMask.show();
	
	// copy the selected domain info into our main data model
	var e = 'The following problems were encountered:';
	var val = i2b2.PM.udlogin.inputUser;
    if (typeof val != 'string') {
        val = val.value;
    }
	if (val) {
		var login_username = val;
	} else {
		e += "\n  Username is empty";
	}
	var val = i2b2.PM.udlogin.inputPass;
    if (typeof val != 'string') {
        val = val.value;
    }
	if (val) {
		var login_password = val;
	} else {
		e += "\n  Password is empty";
	}
	var p = i2b2.PM.udlogin.inputDomain;
	if (typeof p != 'string') {
		var val = p.options[p.selectedIndex].value;
		if (!val.blank()) {
			var p = i2b2.PM.model.Domains;
			if (p[val]) {
				// copy information from the domain record
				var login_domain = p[val].domain;
				var login_url = p[val].urlCellPM;
				i2b2.PM.model.url = login_url;
				var shrine_domain = Boolean.parseTo(p[val].isSHRINE);
				var login_project = p[val].project;
				if (p[val].debug) {
					i2b2.PM.model.login_debugging = p[val].debug;
				} else {
					i2b2.PM.model.login_debugging = false;
				}
			}
		} else {
			e += "\n  No login channel was selected";
		}
	} else {
		var domain_val=i2b2.PM.udlogin.inputDomain;
		var login_domain = domain_val;
		//Assuming localhost was chosen for CAS auth, may need to configure.
		var login_url = "http://localhost:7070/axis2/rest/PMService/";
		i2b2.PM.model.url = login_url;
		var login_project = "unknown project";
		var shrine_domain=false;
		i2b2.PM.model.login_debugging = true;
	}
	
	// call the PM Cell's communicator Object
	var callback = new i2b2_scopedCallback(i2b2.PM._processUserConfig, i2b2.PM);
	var parameters = {
		domain: login_domain, 
		is_shrine: shrine_domain,
		project: login_project,
		username: login_username,
		password: login_password
	};
	var transportOptions = {
		url: login_url,
		user: login_username,
		password: login_password,
		domain: login_domain,
		project: login_project
	};
	i2b2.PM.ajax.getUserAuth("PM:Login", parameters, callback, transportOptions);
}


// ================================================================================================== //
i2b2.PM._processUserConfig = function (data) {
	console.group("PROCESS Login XML");


	// save the valid data that was passed into the PM cell's data model
	i2b2.PM.model.login_username = data.msgParams.sec_user;
	i2b2.PM.model.login_password = data.msgParams.sec_pass;
	i2b2.PM.model.login_domain = data.msgParams.sec_domain;
	i2b2.PM.model.shrine_domain = Boolean.parseTo(data.msgParams.is_shrine);
	i2b2.PM.model.login_project = data.msgParams.sec_project;
	console.info("AJAX Login Successful! Updated: i2b2.PM.model");
	console.dir(i2b2.PM.model);

	// clear the password
	i2b2.PM.udlogin.inputPass.value = "";
	// hide the modal form if needed
	try { i2b2.PM.view.modal.login.hide(); } catch(e) {}
//	alert("cellURL: "+i2b2.PM.model.url);
	i2b2.PM.cfg.cellURL = i2b2.PM.model.url;  // remember the url
	// if user has more than one project display a modal dialog box to have them select one
	var xml = data.refXML;
	var projs = i2b2.h.XPath(xml, 'descendant::user/project[@id]');
	console.debug(projs.length+' project(s) discovered for user');
	if (projs.length == 0) {
		try { i2b2.h.LoadingMask.hide(); } catch(e) {}
		alert("Your account does not have access to any i2b2 projects.");
		try { i2b2.PM.view.modal.login.show(); } catch(e) {}
		return true;
	} else if (projs.length == 1) {
		// default to the only project the user has access to
		i2b2.PM.model.login_project = i2b2.h.XPath(projs[0], 'attribute::id')[0].nodeValue;
		i2b2.PM._processLaunchFramework(xml);
	} else {
		// display list of possible projects for the user to select
		i2b2.PM.view.modal.projectDialog.showProjects(xml);
	}
}

i2b2.PM._processLaunchFramework = function(data) {
	// create signal sender for afterLogin event
	i2b2.events.afterCellInit.subscribe((function(type,args) {
		// keep track of cells loading and fire "afterAllCellsLoaded"
		// event after all cells are confirmed as loaded
		var loadedCells = [];
		for (var i=0; i<i2b2.hive.cfg.LoadedCells.length; i++) {
			try {
				if (i2b2[i2b2.hive.cfg.LoadedCells[i].code]) {
					if (!i2b2[i2b2.hive.cfg.LoadedCells[i].code].isLoaded) {
						// found an unloaded cell, return
						console.warn("Found unloaded cell: "+i2b2.hive.cfg.LoadedCells[i].code);
						return true;
					}
					loadedCells.push(i2b2.hive.cfg.LoadedCells[i].code);
				}
			} catch(e) {
				return true; 
			}
		}
		// all cells are loaded, fire the "all go" signal if any cells are loaded
		if (loadedCells.length == i2b2.hive.cfg.LoadedCells.length) {
			// PM should be referenced back into array
			i2b2.hive.cfg.LoadedCells.push({
					code: "PM", 
					name: i2b2.PM.cfg.config.name,
					forceLoading: true,
					configMsg: {
						id: "PM",
						params:i2b2.PM.cfg.cellParams, 
						url:i2b2.PM.cfg.cellURL
					}
			});
			console.info("EVENT FIRE i2b2.events.afterLogin");
			i2b2.events.afterLogin.fire(loadedCells);
			// remove any cell stubs from memory and the i2b2 namespace
			i2b2.hive.tempCellsList.each(function(o) {
				try {
					if (!i2b2[o.code].isLoaded) { delete i2b2[o.code]; }
				} catch(e) {}
			});
			delete i2b2.hive.tempCellsList;
			
			// hide the "loading" mask
			i2b2.h.LoadingMask.hide();
		}
	}));
	
	// have YUI event subsystem throw errors if we are in debugging mode
//	YAHOO.util.Event.throwErrors = i2b2.PM.model.login_debugging;  <---- this actually breaks some YUI stuff?!


	var cids = [];
	var cobjs = [];
	var c = data.getElementsByTagName('cell_data');
	
    // purge all non-used cells from the local temp cells listing
	var localCellsLoadable = [];
	for (var ic=0; ic < i2b2.hive.cfg.lstCells.length; ic++) {
		// see if the cell is present in the PM cell's list
		var wasPushed = false;
		if (i2b2[i2b2.hive.cfg.lstCells[ic].code] && i2b2[i2b2.hive.cfg.lstCells[ic].code].isLoaded) {
			// the cell is already loaded into memory
			wasPushed = true;
		} else {
			for (var i=0; i<c.length; i++) {
				if (i2b2.hive.cfg.lstCells[ic].code == c[i].getAttribute('id')) {	
					// copy params	
					var cell_rec = {};
					cell_rec.id = c[i].getAttribute('id');
					cell_rec.url = i2b2.h.getXNodeVal(c[i],'url');
					// load any params into an array of data objects 
					var paramsList = [];
					var pl = c[i].getElementsByTagName('param');
					for (var i2=0; i2<pl.length; i2++) {
						var paramSingle = {};
						paramSingle.paramName = pl[i2].getAttribute('name'); 
						paramSingle.paramValue = pl[i2].firstChild.nodeValue; 
						paramsList.push(paramSingle);
					}
					cell_rec.params = paramsList;
					i2b2.hive.cfg.lstCells[ic].configMsg = cell_rec;
					// push info into load array				
					wasPushed = true;
					localCellsLoadable.push(i2b2.hive.cfg.lstCells[ic]);
					break;
				}
			}
		}
		// make sure "forced-load" cells are included regardless of PM cell's list
		if (!wasPushed && i2b2.hive.cfg.lstCells[ic].forceLoading) {
			// generate a configuration message 
			var cref = i2b2.hive.cfg.lstCells[ic];
			if (Object.isUndefined(cref.forceConfigMsg)) { cref.forceConfigMsg = {} }
			cref.forceConfigMsg.id = cref.code;
			cref.configMsg = Object.clone(cref.forceConfigMsg);
			delete cref.forceConfigMsg;
			localCellsLoadable.push(cref);
			wasPushed = true;
		}
	}
	i2b2.hive.cfg.LoadedCells = localCellsLoadable;

	// Initialize the Cell Stubs
	for (var i=0; i<i2b2.hive.cfg.LoadedCells.length; i++) {
		// see if this cell's controller is loaded
		var cell_rec = i2b2.hive.cfg.LoadedCells[i].configMsg;
		if (cell_rec) { 
			if (!cell_rec.id) {
				cell_rec.id = i2b2.hive.cfg.LoadedCells[i].code;
			}
			if (i2b2[cell_rec.id]) {
				if (i2b2[cell_rec.id].Init && !i2b2[cell_rec.id].isLoaded) {
					try {
						i2b2[cell_rec.id].Init(cell_rec.url, cell_rec.params);
					} catch(e) {
						console.error("CELL INITIALIZATION FAILURE! ["+cell_rec.id+"]");
					}
				}
			}
		} else {
			console.error("CELL INITIALIZATION FAILURE! Missing Configuration Message ["+i2b2.hive.cfg.LoadedCells[i].code+"]");
		}
	}
	console.groupEnd("PROCESS Login XML");
}

// ================================================================================================== //
i2b2.PM.doLogout = function() {
	// bug fix - must reload page to avoid dirty data from lingering
	window.location.reload();
}


i2b2.PM.view.modal.projectDialog = {
	loginXML: false,
	showProjects: function(dataXML) {
		var thisRef = i2b2.PM.view.modal.projectDialog;
		thisRef.loginXML = dataXML;
		if (!$("i2b2_projects_modal_dialog")) {
			var htmlFrag = i2b2.PM.model.html.projDialog;
			Element.insert(document.body,htmlFrag);
		
			if (!thisRef.yuiDialog) {
				thisRef.yuiDialog = new YAHOO.widget.SimpleDialog("i2b2_projects_modal_dialog", {
					zindex: 700,
					width: "400px",
					fixedcenter: true,
					constraintoviewport: true,
					close: false
				});
				var kl = new YAHOO.util.KeyListener("i2b2_projects_modal_dialog", { keys:13 },  							
																  { fn:i2b2.PM.view.modal.projectDialog.loadProject,
																	scope:i2b2.PM.view.modal.projectDialog,
																	correctScope:true }, "keydown" );
				thisRef.yuiDialog.cfg.queueProperty("keylisteners", kl);
				thisRef.yuiDialog.render(document.body);
				// show the form
				thisRef.yuiDialog.show();
			}
		}
		// show the form
		thisRef.yuiDialog.show();
		$('loginProjs').focus();
		// load the project data
		var pli = $('loginProjs');
		while( pli.hasChildNodes() ) { pli.removeChild( pli.lastChild ); }
		// populate the Project data into the form
		i2b2.PM.model.projects = {};
		var projs = i2b2.h.XPath(thisRef.loginXML, 'descendant::user/project[@id]');
		for (var i=0; i<projs.length; i++) {
			// save data into model
			var code = projs[i].getAttribute('id');
			i2b2.PM.model.projects[code] = {};
			i2b2.PM.model.projects[code].name = i2b2.h.getXNodeVal(projs[i], 'name');
			// details
			var projdetails = i2b2.h.XPath(projs[i], 'descendant-or-self::param[@name]');
			i2b2.PM.model.projects[code].details = {};
			for (var d=0; d<projdetails.length; d++) {
				var paramName = projdetails[d].getAttribute('name');
				i2b2.PM.model.projects[code].details[paramName] = projdetails[d].firstChild.nodeValue;
			}
			// dropdown
			pno = document.createElement('OPTION');
			pno.setAttribute('value', code);
			var pnt = document.createTextNode(i2b2.PM.model.projects[code].name);
			pno.appendChild(pnt);
			pli.appendChild(pno);			
		}
		// select first project
		$('loginProjs').selectedIndex = 0;

		// display the details for the currently selected project
		i2b2.PM.view.modal.projectDialog.renderDetails();
	},
	renderDetails: function() {
		// clear the details display
		var pli = $('projectAttribs');
		while( pli.hasChildNodes() ) { pli.removeChild( pli.lastChild ); }
		
		// get the currently selected project
		var p = $('loginProjs');
		var projectCode = p.options[p.selectedIndex].value;
		
		// show details
		for (var i in i2b2.PM.model.projects[projectCode].details) {
			// clone the record DIV and add it to the display list
			var rec = $('projDetailRec-CLONE').cloneNode(true);
			// change the entry id
			rec.id = "";
			rec.style.display = "";
			try {
				var part = rec.select('.name')[0];
				part.innerHTML = i;
				part = rec.select('.value')[0];
				part.innerHTML = i2b2.PM.model.projects[projectCode].details[i];
			} catch(e) {}
			pli.appendChild(rec);
		}
		if (!i) {
			Element.insert(pli,'<DIV class="NoDetails">No additional information is available.</DIV>');
		} else {
			Element.insert(pli,'<DIV style="clear:both;"></DIV>');
		}
	},
	loadProject: function(ProjId) {
		if (!ProjId) {
			// get the ID of the currently selected project in the dropdown
			var p = $('loginProjs');
			ProjId = p.options[p.selectedIndex].value;
		}
		i2b2.PM.model.login_project = ProjId;
		i2b2.PM.view.modal.projectDialog.yuiDialog.hide();
		i2b2.PM._processLaunchFramework(i2b2.PM.view.modal.projectDialog.loginXML);
	}
}

console.timeEnd('execute time');
console.groupEnd();