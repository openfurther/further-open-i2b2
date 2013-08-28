/**
 * @projectDescription	Allows user to export a result set.
 * @inherits	i2b2
 * @namespace	i2b2.IRBApplication
 * @author	Shan He
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 09-25-12: 	Initial Launch [Shan He]
 */

i2b2.IRBApplication.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("IRBApplication-PRSDROP", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("IRBApplication-PRSDROP", "PRS", "DropHandler", i2b2.IRBApplication.prsDropped);

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("IRBApplication-TABS", {activeIndex:0});
	this.yuiTabs.on('activeTabChange', function(ev) {
		//Tabs have changed
		if (ev.newValue.get('id')=="IRBApplication-TAB1") {
			// user switched to Results tab
			if (i2b2.IRBApplication.model.prsRecord) {
				// contact PDO only if we have data
				if (i2b2.IRBApplication.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					i2b2.IRBApplication.getResults();
				}
			}
		}
	});
};

i2b2.IRBApplication.Unload = function() {
	// purge old data
	i2b2.IRBApplication.model.prsRecord = false;
	return true;
};

i2b2.IRBApplication.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.IRBApplication.model.prsRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("IRBApplication-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("IRBApplication-PRSDROP").style.background = "#CFB";
	setTimeout("$('IRBApplication-PRSDROP').style.background='#DEEBEF'", 250);
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	//no use in this plugin
	//i2b2.IRBApplication.model.dirtyResultsData = true;

};

i2b2.IRBApplication.showAttributesDialog = function() {
	
		var handleCancel = function() {
			this.cancel();
		};

		var handleSubmit = function() {
			
			var attributes = {};
			
			var dialogElement = $('dialogAttributeSelect');
			var chkAttributeElement=dialogElement.select('INPUT.chkDemoAttribute');
			for ( var i = 0; i < chkAttributeElement.length; i++) {
				attributes['chk_' + chkAttributeElement[i].value] = chkAttributeElement[i].checked;
				
				//if(chkAttributeElement[i].checked)
			}
				
			if(attributes.chk_age)
					alert("you selected age ");
		}
		
		i2b2.IRBApplication.view.dialogAttributeSelect = new YAHOO.widget.SimpleDialog(
				"dialogAttributeSelect", {
					width : "400px",
					fixedcenter : true,
					constraintoviewport : true,
					modal : true,
					zindex : 700,
					buttons : [ {
						text : "OK",
						handler : handleSubmit,
						isDefault : true
					}, {
						text : "Cancel",
						handler : handleCancel
					} ]
				});
		
		//duplicate the following two statements?
		$('dialogAttributeSelect').show();
		i2b2.IRBApplication.view.dialogAttributeSelect.render(document.body);
		
		
		
		
	// manage the event handler for submit
	delete i2b2.IRBApplication.view.dialogAttributeSelect.submitterFunction;
	i2b2.IRBApplication.view.dialogAttributeSelect.submitterFunction = handleSubmit;
	// display the dialoge
	i2b2.IRBApplication.view.dialogAttributeSelect.center();
	i2b2.IRBApplication.view.dialogAttributeSelect.show();
}


i2b2.IRBApplication.getResults = function()
//-------------------------------------------------------------------
// AJAX request and response processing function called upon user click "Export"
//-------------------------------------------------------------------
{
	// callback processor
	var scopedCallback = new i2b2_scopedCallback();
	scopedCallback.scope = this;
	scopedCallback.callback = i2b2.IRBApplication.getResultsCallback;
	
	i2b2.FUR.ajax.getRowLevelPatientData("Plugin:IRBApplication",
	        {resourceName: i2b2_query_id},
	        scopedCallback);
	
}

// Not necessary, send the query id as a resource name and be appended to the request url

//i2b2.IRBApplication._getIRBApplicationRequestXML = function(queryId)
////-------------------------------------------------------------------
//// This function is used to build the data export request in XML
////-------------------------------------------------------------------
//{
//	var i2b2_query_id = i2b2.IRBApplication.model.prsRecord.origData.QI_id;
//
//	var request = '<query_id>'+i2b2_query_id+'</query_id>\n';
//	s+='<user_id'+i... +'</user_id>\n';
//	
//	
//}

i2b2.IRBApplication.getResultsCallback = function (ajaxResponse)

//-------------------------------------------------------------------
{

}

