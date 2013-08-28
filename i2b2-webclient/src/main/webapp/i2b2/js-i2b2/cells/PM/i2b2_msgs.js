/**
 * @projectDescription	Messages used by the PM cell communicator object.
 * @inherits 	i2b2.PM.cfg
 * @namespace	i2b2.PM.cfg.msgs
 * @author		Nick Benik, Griffin Weber MD PhD
 * @version 	1.3
 * ----------------------------------------------------------------------------------------
 * updated 9-15-08: RC4 launch [Nick Benik] 
 */

i2b2.PM.model.attemptingLogin = false;

i2b2.PM.cfg.msgs = {};	
// ================================================================================================== //
i2b2.PM.cfg.msgs.getUserAuth = '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'+
'<i2b2:request xmlns:i2b2="http://www.i2b2.org/xsd/hive/msg/1.1/" xmlns:pm="http://www.i2b2.org/xsd/cell/pm/1.1/">\n'+
'    <message_header>\n'+
'        {{{proxy_info}}}\n'+
'        <i2b2_version_compatible>1.1</i2b2_version_compatible>\n'+
'        <hl7_version_compatible>2.4</hl7_version_compatible>\n'+
'        <sending_application>\n'+
'            <application_name>i2b2 Project Management</application_name>\n'+
'            <application_version>1.1</application_version>\n'+
'        </sending_application>\n'+
'        <sending_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </sending_facility>\n'+
'        <receiving_application>\n'+
'            <application_name>Project Management Cell</application_name>\n'+
'            <application_version>1.1</application_version>\n'+
'        </receiving_application>\n'+
'        <receiving_facility>\n'+
'            <facility_name>i2b2 Hive</facility_name>\n'+
'        </receiving_facility>\n'+
'        <datetime_of_message>2007-04-09T15:19:18.906-04:00</datetime_of_message>\n'+
'		<security>\n'+
'			<domain>{{{sec_domain}}}</domain>\n'+
'			<username>{{{sec_user}}}</username>\n'+
'			<password>{{{sec_pass}}}</password>\n'+
'		</security>\n'+
'        <message_control_id>\n'+
'            <message_num>{{{header_msg_id}}}</message_num>\n'+
'            <instance_num>0</instance_num>\n'+
'        </message_control_id>\n'+
'        <processing_id>\n'+
'            <processing_id>P</processing_id>\n'+
'            <processing_mode>I</processing_mode>\n'+
'        </processing_id>\n'+
'        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\n'+
'        <application_acknowledgement_type>AL</application_acknowledgement_type>\n'+
'        <country_code>US</country_code>\n'+
'        <project_id>{{{sec_project}}}</project_id>\n'+
'    </message_header>\n'+
'    <request_header>\n'+
'        <result_waittime_ms>{{{result_wait_time}}}000</result_waittime_ms>\n'+
'    </request_header>\n'+
'    <message_body>\n'+
'        <pm:get_user_configuration>\n'+
'            <project>{{{sec_project}}}</project>\n'+
'        </pm:get_user_configuration>\n'+
'    </message_body>\n'+
'</i2b2:request>';


// ================================================================================================== //



// create the communicator Object
i2b2.PM.ajax = i2b2.hive.communicatorFactory("PM");
i2b2.PM.ajax._addFunctionCall("getUserAuth","{{{URL}}}getServices", i2b2.PM.cfg.msgs.getUserAuth);

