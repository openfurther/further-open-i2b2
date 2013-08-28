/**
 * @projectDescription	Allows user to export a result set.
 * @inherits	i2b2
 * @namespace	i2b2.DataExport
 * @author	Shan He
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 09-25-12: 	Initial Launch [Shan He]
 */

i2b2.DataExport.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("DataExport-PRSDROP", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("DataExport-PRSDROP", "PRS", "DropHandler", i2b2.DataExport.prsDropped);

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("DataExport-TABS", {activeIndex:0});
};

i2b2.DataExport.Unload = function() {
	// purge old data
	i2b2.DataExport.model.prsRecord = false;
	return true;
};

i2b2.DataExport.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.DataExport.model.prsRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("DataExport-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("DataExport-PRSDROP").style.background = "#CFB";
	setTimeout("$('DataExport-PRSDROP').style.background='#DEEBEF'", 250);
	//Enable the export button if it was disabled due to a previous export
	if($("exportButton").disabled==true){
		$("exportButton").removeAttribute("disabled");
		$("exportButton").setValue("Export to CSV");

	}
	//check if the query exists, call a restful service
	// callback processor
//	var scopedCallback = new i2b2_scopedCallback();
//	scopedCallback.scope = this;
//	scopedCallback.callback = i2b2.DataExport.QueryExistValidation;
//	
//	var i2b2QueryId = i2b2.DataExport.model.prsRecord.origData.QI_id;
//	i2b2.FUR.ajax.validateQueryByI2b2OrigId("Plugin:DataExport",
//	        {resourceName: i2b2QueryId},
//	        scopedCallback);
	//if the above code does not work, then try to use Prototype Ajax call directly
	

};

i2b2.DataExport.QueryExistValidation = function(result)
{
	alert(results);//the result should be in json format
	alert("The query you selected does not exist in the query engine anymore for security reasons. Please run the query again before you do data export.");
	//clear prsRecord
	$("DataExport-PRSDROP").innerHTML="Drop a Patient Set here";
	i2b2.DataExport.model.prsRecord=false;
}

i2b2.DataExport.preExport = function()
//-------------------------------------------------------------------
// Form validation and msg construction after click
//-------------------------------------------------------------------
{
	if(i2b2.DataExport.model.prsRecord ){
		
		var submitButton=$("exportButton");
		submitButton.setValue("Processing...");
		submitButton.disabled=true;
		var i2b2QueryId = i2b2.DataExport.model.prsRecord.origData.QI_id;
		var userId=i2b2.h.getUser();
		var exportRequestMsg= '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
							  '		<exportContext xmlns="http://further.utah.edu/fqe" xmlns:query="http://further.utah.edu/core/query" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\n'+
							  '			<query_id>'+i2b2QueryId+'</query_id>\n'+
							  '			<user_id>'+userId+'</user_id>\n'+
							  '		</exportContext>\n';
		var hiddenElement =$("xmlMsg");
		hiddenElement.setValue(exportRequestMsg);

		return true;
	
	}else{
		
		alert("Please drop a patient data set.");
		return false;
		
	}
	
}

i2b2.DataExport.reset = function(){
	$("DataExport-PRSDROP").innerHTML="Drop a Patient Set here";
	if($("exportButton").disabled==true){
		$("exportButton").removeAttribute("disabled");
		$("exportButton").setValue("Export to CSV");

	}
	//clear prsRecord
	i2b2.DataExport.model.prsRecord=false;

}



