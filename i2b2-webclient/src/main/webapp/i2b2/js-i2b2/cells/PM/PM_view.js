/**
 * @projectDescription	View controller for PM module's login form(s).
 * @inherits 	i2b2
 * @namespace	i2b2.PM
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */
console.group('Load & Execute component file: cells > PM > view');
console.time('execute time');


// login screen
// ================================================================================================== //
i2b2.PM.doConnectForm = function(inputUser, inputPass, inputDomain, inputSubmit) {
	console.debug("i2b2.PM.doConnectForm(",inputUser, inputPass, inputDomain, inputSubmit,")");
	i2b2.PM.udlogin = {};
	// function used to save references to the inputs that make up the login screen
	var ref = i2b2.PM.udlogin;
	ref.inputUser = inputUser;
	ref.inputPass = inputPass;
	ref.inputDomain = inputDomain;
	ref.inputSubmitBtn = inputSubmit;
	YAHOO.util.Event.addListener(inputSubmit.id, "click", i2b2.PM.doLogin); 
	i2b2.PM._redrawConnectedForm();
}

// ================================================================================================== //
i2b2.PM._redrawConnectedForm = function() {
	var ref = i2b2.PM.udlogin;
	// repopulate the domain information
//	ref.inputUser.value = '';
//	ref.inputPass.value = '';
	// clear the list
	var dli = ref.inputDomain;
	while( dli.hasChildNodes() ) { dli.removeChild( dli.lastChild ); }
	// populate the Categories from the data model
	var dml = i2b2.PM.model.Domains;
	for (var i=0; i<dml.length; i++) {
		// ONT options dropdown
		dno = document.createElement('OPTION');
		dno.setAttribute('value', i);
		var dnt = document.createTextNode(dml[i].name);
		dno.appendChild(dnt);
		dli.appendChild(dno);
	}
}

// ================================================================================================== //
i2b2.PM.doLoginDialog = function() {
	// this displays the login dialogue box (auto generated popup)
	if (!$("i2b2_login_modal_dialog")) {
		var htmlFrag = i2b2.PM.model.html.loginDialog;
		Element.insert(document.body,htmlFrag);
		
		if (!i2b2.PM.view.modal.login) {
			i2b2.PM.view.modal.login = new YAHOO.widget.Panel("i2b2_login_modal_dialog", {
				zindex: 700,
				width: "501px",
				fixedcenter: true,
				constraintoviewport: true,
				close: false,
				draggable: true
			});
			var kl = new YAHOO.util.KeyListener("i2b2_login_modal_dialog", { keys:13 },  							
																  { fn:i2b2.PM.doLogin,
																	scope:i2b2.PM.view.modal.login,
																	correctScope:true }, "keydown" );
			i2b2.PM.view.modal.login.cfg.queueProperty("keylisteners", kl);
			i2b2.PM.view.modal.login.render(document.body);

			// show the form
			i2b2.PM.view.modal.login.show();
			$('loginusr').focus();
			// connect the form to the PM controller
			i2b2.PM.udlogin = {};
			i2b2.PM.udlogin.inputUser = $('loginusr');
			i2b2.PM.udlogin.inputPass = $('loginpass');
			i2b2.PM.udlogin.inputDomain = $('logindomain');
			// load the domains
			i2b2.PM._redrawConnectedForm();
		}
	}
	// show the form
	i2b2.PM.view.modal.login.show();
}

//========================================================By SH=================================================

i2b2.PM.doAutoLogin = function(user,pass,domain) {
			
			i2b2.PM.udlogin = {};
//			i2b2.PM.udlogin.inputUser = $('loginusr');
			//i2b2.PM.udlogin.inputUser = "i2b2";
			i2b2.PM.udlogin.inputUser = user;
//			i2b2.PM.udlogin.inputPass = $('loginpass');
			//i2b2.PM.udlogin.inputPass = "demouser";
			i2b2.PM.udlogin.inputPass = pass;
//			i2b2.PM.udlogin.inputDomain = $('logindomain');
			//i2b2.PM.udlogin.inputDomain ="i2b2.org";
			i2b2.PM.udlogin.inputDomain =domain;
			//i2b2.PM.udlogin.inputDomain ="HarvardDemo";
			// load the domains
			//i2b2.PM._redrawConnectedForm();
			i2b2.PM.doLogin();
}
	//}
//======================================================end==========================================================


console.timeEnd('execute time');
console.groupEnd();