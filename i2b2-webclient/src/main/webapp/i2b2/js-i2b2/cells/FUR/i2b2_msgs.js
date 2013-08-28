//*********************************************************************************
// Added by Dustin Schultz for FURTHeR 02/14/11
//*********************************************************************************
i2b2.FUR.ajax = i2b2.hive.communicatorFactory("FUR");

//create namespaces to hold all the communicator messages and parsing routines
i2b2.FUR.cfg.msgs = {};
i2b2.FUR.cfg.parsers = {};

// Note: all FURTHeR calls below should use the following proxy request. All is does is redirect
// the request into the remote server URL specified in proxy_info. No parsing is performed
// within _addFunctionCall() - the raw XML response is returned to the caller. The last three
// parameters passed to it should be i2b2.FUR.cfg.msgs.ProxyRequest, null, null.

i2b2.FUR.cfg.msgs.ProxyRequest =
'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<request>\n'+
'	<message_header>\n'+
'		{{{proxy_info}}}\n'+
'	</message_header>\n'+
'</request>';

//==================================================================================================
// Get FURTHeR data source metadata
i2b2.FUR.ajax._addFunctionCall(	"GetDatasourceInfo",
				"{{{URL}}}mdr/rest/asset/resource/path/i2b2/",
				i2b2.FUR.cfg.msgs.ProxyRequest,
				null,
				null);

//==================================================================================================
// Get aggregated counts of an FQE query triggered from the i2b2 front end

	i2b2.FUR.ajax._addFunctionCall(	"getPDO_fromInputList",
					"{{{URL}}}fqe/rest/fqe/query/count/origin/",
					i2b2.FUR.cfg.msgs.ProxyRequest,
					null,
					null);

//==================================================================================================
// Check if an i2b2 query still exists in esb

	i2b2.FUR.ajax._addFunctionCall(	"validateQueryByI2b2OrigId",
			"{{{URL}}}fqe/rest/fqe/query/status/origin/",
			i2b2.FUR.cfg.msgs.ProxyRequest,
			null,
			null);
