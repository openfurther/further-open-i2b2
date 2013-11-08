/**
 * @projectDescription	Displays demographic information for a single patient set.
 * @inherits	i2b2
 * @namespace	i2b2.Dem1Set
 * @author	Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 12-22-08: 	Initial Launch [Griffin Weber]
 */

i2b2.Dem1Set.Init = function(loadedDiv) {
	// register DIV as valid DragDrop target for Patient Record Sets (PRS) objects
	var op_trgt = {dropTarget:true};
	i2b2.sdx.Master.AttachType("Dem1Set-PRSDROP", "PRS", op_trgt);
	// drop event handlers used by this plugin
	i2b2.sdx.Master.setHandlerCustom("Dem1Set-PRSDROP", "PRS", "DropHandler", i2b2.Dem1Set.prsDropped);

	// manage YUI tabs
	this.yuiTabs = new YAHOO.widget.TabView("Dem1Set-TABS", {activeIndex:0});
	this.yuiTabs.on('activeTabChange', function(ev) {
		//Tabs have changed
		if (ev.newValue.get('id')=="Dem1Set-TAB1") {
			// user switched to Results tab
			if (i2b2.Dem1Set.model.prsRecord) {
				// contact PDO only if we have data
				if (i2b2.Dem1Set.model.dirtyResultsData) {
					// recalculate the results only if the input data has changed
					i2b2.Dem1Set.getResults();
				}
			}
		}
	});
};

i2b2.Dem1Set.Unload = function() {
	// purge old data
	i2b2.Dem1Set.model.prsRecord = false;
	return true;
};

i2b2.Dem1Set.prsDropped = function(sdxData) {
	sdxData = sdxData[0];	// only interested in first record
	// save the info to our local data model
	i2b2.Dem1Set.model.prsRecord = sdxData;
	// let the user know that the drop was successful by displaying the name of the patient set
	$("Dem1Set-PRSDROP").innerHTML = i2b2.h.Escape(sdxData.sdxInfo.sdxDisplayName);
	// temporarly change background color to give GUI feedback of a successful drop occuring
	$("Dem1Set-PRSDROP").style.background = "#CFB";
	setTimeout("$('Dem1Set-PRSDROP').style.background='#DEEBEF'", 250);
	// optimization to prevent requerying the hive for new results if the input dataset has not changed
	i2b2.Dem1Set.model.dirtyResultsData = true;

};

i2b2.Dem1Set.getResults = function()
//-------------------------------------------------------------------
// AJAX request and response processing function called upon
// switching to the "View Results" tab.
//-------------------------------------------------------------------
{
	if (i2b2.Dem1Set.model.dirtyResultsData)
	{
		// callback processor
		var scopedCallback = new i2b2_scopedCallback();
		scopedCallback.scope = this;
		scopedCallback.callback = i2b2.Dem1Set.getResultsCallback;

		// Switch ot the "Specify Data" tab? (hmm or something else??)
	        $$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-directions")[0].hide();
        	$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-finished")[0].hide();
	        $$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-working")[0].show();

		// AJAX CALL USING THE FURTHeR CELL COMMUNICATOR
		// Fetch i2b2 query from i2b2 patient result set and send to callback

		var i2b2_query_id = i2b2.Dem1Set.model.prsRecord.origData.QI_id;
		i2b2.FUR.ajax.getPDO_fromInputList("Plugin:Dem1Set",
	        {resourceName: i2b2_query_id},
	        scopedCallback);

	} // if dirtiesResults
} // i2b2.Dem1Set.getResults()

i2b2.Dem1Set.getResultsCallback = function(results)
//-------------------------------------------------------------------
// This function is used to process the AJAX results of the getChild
// call results data object contains the following attributes:
//	refXML: xmlDomObject <--- for data processing
//	msgRequest: xml (string)
//	msgResponse: xml (string)
//	error: boolean
//	errorStatus: string [only with error=true]
//	errorMsg: string [only with error=true]
//-------------------------------------------------------------------
{
//=================================
// TODO:
// - one div per federated result type
// - sort results on the FQE side
//=================================

	// Switch to the "View Results" tab 
	$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-working")[0].hide();
	$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-finished")[0].show();
	
	if (results.error) {
		var e = '';
		e += '<div class="Dem1Set-MainContentPad">';
		e += 'An error occurred, please check the following:';
		e += '<ul>';
		e += '<li>Check that your query is not a Count Only query. Full data results must be retrieved in order to perform aggregation.</li>'
		e += '<li>Check that your query was recently ran. Results are stored in memory and only exist for a limited amount of time.</li>'
		e += '</ul>';
		e += '</div>';
		$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-finished")[0].innerHTML = e;
		return;
	}

	//-----------------------------------
	// Load data
	//-----------------------------------
	i2b2.Dem1Set.loadDataIntoModel(results.refXML);

	//-----------------------------------
	// Print title lines
	//-----------------------------------
	var s = '';
	s += '<div class="intro">';
	s += 'Below are the demographic details for the selected patient set. ';
	s += 'For each demographic category, the values, number of patients, and a histogram are shown.';
	s += '</div>';
	s += '<div class="resultLV">';

	s += '<div class="resultLbl">Patient Set:</div>';
	s += '<div class="resultVal">'
		+ i2b2.Dem1Set.model.prsRecord.sdxInfo.sdxDisplayName
		+ '</div>';

	s += '<div class="resultLbl">Query ID:</div>';
	var dataSourcesStr = 'sources';
	if (i2b2.Dem1Set.model.numDataSources == 1) {
		dataSourcesStr = 'source';
	}
	s += '<div class="resultVal">'
		+ i2b2.Dem1Set.model.prsRecord.origData.QI_id
		+ ', to which ' + i2b2.Dem1Set.model.numDataSources + ' ' + dataSourcesStr + ' responded'
		+ '</div>';

	s += '</div>'
		
	s += '<div class="resultDagger">';
	s += '&#8224; Aggregation of these data is not possible in some instances and will be categorized as \'Missing Data\'';
	s += '</div>';

	s += '<br/>';

	s += '<div class="demTables">';

	//------------------------------------
	// Print table: total federated counts
	//------------------------------------
        s += '<div class="sectionTitle">' + 'Total Counts' + '</div>';
	s += i2b2.Dem1Set.drawTable(null,
		i2b2.Dem1Set.model.sumCounts, i2b2.Dem1Set.maxValue(i2b2.Dem1Set.model.sumCounts));

	//------------------------------------
	// print tables for each result view
	//------------------------------------
	for (var rv = 0; rv < i2b2.Dem1Set.model.rvCounts.length; rv++)
	{
		var rvEntry = i2b2.Dem1Set.model.rvCounts[rv];
                s += '<div class="sectionTitle">' + rvEntry.key + '</div>';
		var rvData  = rvEntry.value;

		//----------------------------------------------------
		// Print Tables for each dem category that has results
		//----------------------------------------------------
		for (var cat = 0; cat < rvData.length; cat++)
		{
			var demCatData = rvData[cat].value;
			s += i2b2.Dem1Set.drawTable(rvEntry.key + ' - ' + rvData[cat].key,
				demCatData, i2b2.Dem1Set.totalValue(demCatData));
		}
	}

	s += '</div>';

	s += '<br/>';

	// Insert the entire constructed content into the page's dynamic DOM tree
	$$("DIV#Dem1Set-mainDiv DIV#Dem1Set-TABS DIV.results-finished")[0].innerHTML = s;

	// optimization - only requery when the input data is changed
	i2b2.Dem1Set.model.dirtyResultsData = false;
} // i2b2.Dem1Set.getResultsCallback()

i2b2.Dem1Set.loadDataIntoModel = function(ajaxResponse)
//-------------------------------------------------------------------
// Load the aggregated result data from the AJAX XML response into
// this plugin's model.
// Parameters:
//	ajaxResponse: xmlDomObject <--- for data processing
// Upon return, i2b2.Dem1Set.model is updated with the data parsed
// from ajaxResponse
//-------------------------------------------------------------------
{
	// Get total aggregated counts (=result views or RVs)
	// Save in the Plugin's data model
	var aggregatedResults = ajaxResponse.firstChild;
	if (Prototype.Browser.IE) {
		aggregatedResults = aggregatedResults.nextSibling;
	}
	var rvData = aggregatedResults.childNodes[aggregatedResults.childNodes.length-3].childNodes;
	i2b2.Dem1Set.model.sumCounts = i2b2.Dem1Set.getResultsViews(rvData);

	// Load other global result fields
	var numDataSources = aggregatedResults.childNodes[aggregatedResults.childNodes.length-2].textContent;
	if (Prototype.Browser.IE) {
		numDataSources = aggregatedResults.childNodes[aggregatedResults.childNodes.length-2].text;
	}
	i2b2.Dem1Set.model.numDataSources = parseInt(numDataSources);

	// Load data for each demographic category into a hash table that
	// maps federated result type -> (hash of dem cat -> histogram)
	// Note: process all childNodes <aggregatedResult> elements of
	// the root tag <aggregatedResults> except the last three, which are
	// <resultView>, <numDataSources>, <result_status>
	var numIgnoredLastElements = 3;

	
	var aggregatedResultElements = aggregatedResults.childNodes;
	var numResultViews = aggregatedResultElements.length-numIgnoredLastElements;
	var rvCounts = new Array(numResultViews);
	var index = 0;
	for (var i = 0; i < numResultViews; i++)
	{
		var rvElement = aggregatedResultElements[i];

		// Read and prepare result view key
		var keyElement 		= rvElement.firstChild;
		var type 		= keyElement.getAttribute('type');

		rvCounts[index] 	= new Object();
		rvCounts[index].key 	= i2b2.Dem1Set.resultViewDisplayName(type)
		rvCounts[index].value 	= i2b2.Dem1Set.getResultViewBreakdown(rvElement.childNodes);
		
		index++;
	}

	// Save in the Plugin's data model
	i2b2.Dem1Set.model.rvCounts = rvCounts;
}

i2b2.Dem1Set.getResultsViews = function(rvData)
//----------------------------------------------------------------------
// Retrieve total counts (ResultViews) from FQE web service response.
// Parameters:
// rvData - list of result view <entry> DOM elements
// Returns: histogram of (result view key, count), where key =
//          a unique identifier = display name of a result view
//----------------------------------------------------------------------
{
	var rv = new Array(rvData.length);
	for (var i = 0; i < rvData.length; i++)
	{
		var rvElement		= rvData[i];
		var key 		= rvElement.childNodes[0];
		var type 		= key.textContent;
		if (Prototype.Browser.IE) {
			type 		= key.text;
		}
		var numRecords 		= rvElement.childNodes[1].firstChild.textContent;
		if (Prototype.Browser.IE) {
			numRecords 		= rvElement.childNodes[1].firstChild.text;
		}
		rv[i] 			= new Object();
		rv[i].key 		= i2b2.Dem1Set.resultViewDisplayName(type);
		rv[i].value		= parseInt(numRecords);
	}
	return rv;
}

i2b2.Dem1Set.getResultViewBreakdown = function(rvData)
//----------------------------------------------------------------------
// Build a hash map of (dem category key -> dem category histogram)
// Parameters:
// rvData - list of childNodes elements of a <aggregatedResult> DOM element
// Returns: map: dem cat key -> dem cat histogram (category)
//          a unique identifier = display name of a result view
//----------------------------------------------------------------------
{
	// Note: skip first element because it is a <resultContentKey>;
	// the rest are <category>s
        var rv = new Array(rvData.length-1);
	var index = 0;
        for (var i = 1; i < rvData.length; i++)
        {
                var rvElement = rvData[i];
		var key = rvElement.getAttribute('name');

		// Get all values/entry elements and build a single category's map
		var values = rvElement.firstChild;
		var category = new Array(0);
		if (values != null)
		{
			var entries = values.childNodes;
			var category = new Array(entries.length);
			for (var j = 0; j < entries.length; j++)
			{
		        	var entry 		= entries[j];
				category[j] 		= new Object();
        		        category[j].key 	= entry.getAttribute('key');
				category[j].value 	= parseInt(entry.getAttribute('value'));
			}
		}

		// Save category in the result view map. Preserve XML element order
		// in the map entries
		rv[index] 	= new Object();
		rv[index].key   = key;
       	        rv[index].value = category;
		index++;
        }
        return rv;
}

i2b2.Dem1Set.resultViewDisplayName = function(type)
//----------------------------------------------------------------------
// Generate a human-readable string of a result view.
// Parameters:
// type - sum/union/intersection/...
// Returns: result view display name
//----------------------------------------------------------------------
{
	switch (type)
	{
		case 'SUM':
		{
		  	return 'Patient Sum'
		}
		case 'UNION':
		{
			return 'Unique Patients';
		}
		case 'INTERSECTION':
		{
			return 'Patients Common in All Sources';
		}
		default:
		{
			return 'Unknown Result';
		}
	}
}

i2b2.Dem1Set.drawTable = function(title, rv, maxVal)
//----------------------------------------------------------------------
// Draw a single demographics histogram table. Includes counts and bars.
// Parameters:
// title - table title.
// rv - histogram data
// maxVal - maximum histogram value to scale bars
// Returns: an HTML string containing the histogram table
//----------------------------------------------------------------------
{
	var s = '';
	if (title != null)
	{
		s += '<div class="demcatTitle">' + title + '</div>';
	}
	if (rv.length == 0)
	{
		s += '<div class="intro">No results.</div>';
	}
 	else
	{
		s += '<table>';
        	for (var i = 0; i < rv.length; i++)
		{
			var key 	= rv[i].key;
			var value	= rv[i].value;
	           	var barWidth 	= 200 * (value/maxVal);
			var valueStr = '' + value;
			if (value < 0)
			{
				// Scrubbed small value
				barWidth = 0;
				valueStr = '*';
			}
			if (key == '-')
			{
				key = 'Missing Data';
			}
        		s += '<tr>';
                	s += '<th>' + key + '</th>';
	                s += '<td>' + valueStr + '</td>';
        	        s += '<td class="barTD"><div class="bar" style="width:' + barWidth + 'px;"></div></td>';
	                s += '</tr>';
        	}
	        s += '</table>';
	}
        s += '<br/>';
	return s;
}

i2b2.Dem1Set.maxValue = function(x)
//----------------------------------------------------------------------
// Return the maximum value in a histogram x.
//----------------------------------------------------------------------
{
	var maxVal = 0;
        for (var i = 0; i < x.length; i++)
	{
		var tempVal = x[i].value;
                if (tempVal > maxVal) {maxVal = tempVal;}
        }
        maxVal *= 1.0;
	return maxVal;
}

i2b2.Dem1Set.totalValue = function(x)
//----------------------------------------------------------------------
// Return the sum of values in a histogram x.
//----------------------------------------------------------------------
{
	var maxVal = 0;
        for (var i = 0; i < x.length; i++)
	{
		var tempVal = x[i].value;
                maxVal += tempVal;
        }
        maxVal *= 1.0;
	return maxVal;
}
