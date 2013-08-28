<%@ page import="org.gridlab.gridsphere.portlet.PortletGroup" %>
<%@ taglib uri="/portletUI" prefix="ui" %>
<%@ taglib uri="/portletAPI" prefix="portletAPI" %>

<portletAPI:init/>

<portlet:defineObjects/>



<style type="text/css">
#param_new {
	width: 500px;
	float:left;
	visibility:hidden; 
	border-left:1px solid #ccc;
	margin-left:10px;
	padding-left:10px;
}
#param_new3 {
	padding-top: 6px;
	visibility:hidden; 
}
.line {
	line-height:4px; 
	margin-bottom: 8px; 
	border-bottom: 1px dotted #585858;
	height: 5px;
}
.missing {
	font-clolor: #e83e00;
}

select {
	width: 300px;
}
</style>
<script type="text/javascript">


function enableDiv() {

overl3 = document.forms[0].ui_lb_userdatalist_;
overl3.style.color = 'black';
overl3.disabled = false;
overl3 = document.getElementById('delete');
overl3.style.color = 'black';
overl3.disabled = false;
overl3 = document.forms[0].add;
overl3.style.color = 'black';
overl3.disabled = false;

}

function disableDiv(elm) {

overl3 = document.forms[0].ui_lb_userdatalist_;
overl3.style.color = 'gray';
overl3.disabled = true;
overl3 = document.getElementById('delete');
overl3.style.color = 'gray';
overl3.disabled = true;
overl3 = document.forms[0].add;
overl3.style.color = 'gray';
overl3.disabled = true;

}

 

function ShowHide2(szDivID, iState)
{

    if(document.layers)	   //NN4+
    {
       document.layers[szDivID].visibility = iState ? "show" : "hide";
    }
    else if(document.getElementById)	  //gecko(NN6) + IE 5+
    {
        var obj = document.getElementById(szDivID);
        obj.style.visibility = iState ? "visible" : "hidden";
    }
    else if(document.all)	// IE 4
    {
        document.all[szDivID].style.visibility = iState ? "visible" : "hidden";
    }

}


function clearNameValueError()
{
  		document.getElementById('rightname2').style.visibility = "hidden";
  		document.getElementById('rightname2').style.borderTop = "";
  		document.getElementById('rightname2').style.borderBottom = "";
    		document.getElementById('rightname2').style.borderRight= ""
  		document.getElementById('leftname2').style.borderTop = "";
  		document.getElementById('leftname2').style.borderBottom = "";
    		document.getElementById('leftname2').style.borderLeft= "";
    		document.getElementById('centername2').style.borderTop = "";
  		document.getElementById('centername2').style.borderBottom = "";
  		
  		document.getElementById('rightvalue').style.visibility = "hidden";
  		document.getElementById('rightvalue').style.borderTop = "";
  		document.getElementById('rightvalue').style.borderBottom = "";
    		document.getElementById('rightvalue').style.borderRight= "";
  		document.getElementById('leftvalue').style.borderTop = "";
  		document.getElementById('leftvalue').style.borderBottom = "";
    		document.getElementById('leftvalue').style.borderLeft= "";  
    		document.getElementById('centervalue').style.borderTop = "";
  		document.getElementById('centervalue').style.borderBottom = "";  		
}

function doSubmit(form)
{
  var errors = false;
  
  if (document.getElementById('param_new').style.visibility == "visible")
  {
	  if (form.ui_tf_name_.value == "")  	
		{
			document.getElementById('rightname').style.visibility = "visible";
			document.getElementById('rightname').style.borderTop = "1px solid #e83e00";
			document.getElementById('rightname').style.borderBottom = "1px solid #e83e00";
			document.getElementById('rightname').style.borderRight= "1px solid #e83e00";
			document.getElementById('leftname').style.borderTop = "1px solid #e83e00";
			document.getElementById('leftname').style.borderBottom = "1px solid #e83e00";
			document.getElementById('leftname').style.borderLeft= "1px solid #e83e00";  
			document.getElementById('centername').style.borderTop = "1px solid #e83e00";
			document.getElementById('centername').style.borderBottom = "1px solid #e83e00";
			errors = true;
		}
	  if (form.ui_lb_userlist_.selectedIndex < 0)  		
		{
			document.getElementById('rightuser').style.visibility = "visible";
			document.getElementById('rightuser').style.borderTop = "1px solid #e83e00";
			document.getElementById('rightuser').style.borderBottom = "1px solid #e83e00";
			document.getElementById('rightuser').style.borderRight= "1px solid #e83e00";
			document.getElementById('leftuser').style.borderTop = "1px solid #e83e00";
			document.getElementById('leftuser').style.borderBottom = "1px solid #e83e00";
			document.getElementById('leftuser').style.borderLeft= "1px solid #e83e00";  
			document.getElementById('centeruser').style.borderTop = "1px solid #e83e00";
			document.getElementById('centeruser').style.borderBottom = "1px solid #e83e00";
			errors = true;
		}
	  if (form.ui_tf_value_.value == "")  	
		{
			document.getElementById('rightvalue').style.visibility = "visible";
			document.getElementById('rightvalue').style.borderTop = "1px solid #e83e00";
			document.getElementById('rightvalue').style.borderBottom = "1px solid #e83e00";
			document.getElementById('rightvalue').style.borderRight= "1px solid #e83e00";
			document.getElementById('leftvalue').style.borderTop = "1px solid #e83e00";
			document.getElementById('leftvalue').style.borderBottom = "1px solid #e83e00";
			document.getElementById('leftvalue').style.borderLeft= "1px solid #e83e00";  
			document.getElementById('centervalue').style.borderTop = "1px solid #e83e00";
			document.getElementById('centervalue').style.borderBottom = "1px solid #e83e00";
			errors = true;
		}
	} else {
	  var elSel = document.getElementById('selectX');
	  var i;
	  var count = 0;
	  var str = "";
	  var addHidden = document.getElementById('param_new3');
	  for (i = elSel.length - 1; i>=0; i--) {
		if (elSel.options[i].text.indexOf('=') !=-1)
		{
			addHidden.innerHTML += '<INPUT TYPE="hidden" NAME="ui_tf_name' + count+ '_" VALUE="' + elSel.options[i].text + '">';  	
			count++;
		}
	  }
	}

  if (errors == false) 
  {
  	return true;
  }
  else {
  	return false;
  }
}



var count1 = 0;
var count2 = 0;



function removeOptionSelected()
{
  var elSel = document.getElementById('selectX');
  var i;
  for (i = elSel.length - 1; i>=0; i--) {
    if (elSel.options[i].selected) {
      elSel.remove(i);
    }
  }
}
</script>


<ui:form name="mike" onSubmit="return doSubmit(this);">
<table>
<tr><td valign="top">
<div id="col">
	<img src="/default/html/images/hive-small.gif"><B>Manage User Variables</b>
	<div class="line">&nbsp;</div>
	<ui:text value="Register new user variables or delete existing ones:"/>
	<br />


	<div id="dyn_table" name="dyn_table" class="channel_fields">
	        <ui:listbox id="mike" beanId="userdatalist"  size="5" >
		</ui:listbox>

		<br clear="all"/>
		<input id="delete" class="portlet-form-button" type="submit" value="Remove Selected" name="gs_action=deleteUserData"/> 
		<br clear="all"/>
		<div class="line">&nbsp;</div>

		<input type=button name="add" value="Add New User Variable -->" onclick="disableDiv(); ShowHide2('param_new', 1)">
	</div>
</div>
</td>
<td valign="top">
 <div id="param_new" name="parm_new">
		<img src="/default/html/images/cell-small.gif"><B>Add User Variable</B>
		<div class="line">&nbsp;</div>
		<table>
		<tr name="chkUser" class="col1">
		<td id="leftuser"><ui:text value="User:"/></td>
	        <td id="centeruser">
		 			<ui:listbox beanId="userlist" size="5"/>
		 	</td>
		<td id="rightuser" style="visibility:hidden; ">
			<img src="/default/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
		</td>
		</tr>
		<tr name="chkName" class="col1">
		<td id="leftname"><ui:text value="Name:"/></td>
	        <td id="centername"><ui:textfield beanId="name"/></td>
		<td id="rightname" style="visibility:hidden; ">
			<img src="/default/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
		</td>
		</tr>
		<tr name="chkValue" class="col1">
		<td id="leftvalue"><ui:text value="Value:"/></td>
		<td id="centervalue"><ui:textfield beanId="value"/></td>
		<td id="rightvalue" style="visibility:hidden; ">
			<img src="/default/html/images/missing.gif">&nbsp;<font color="#E83E00">This field is missing</font>
		</td>
		</table>

		<div class="line">&nbsp;</div>
	 		<ui:actionsubmit  action="addUserData" value="Save"/> 
 			<input type=button value="Cancel" onclick="enableDiv(); ShowHide2('param_new', 0)">
		</div>
</div>	 
</td></tr>
</table>
</ui:form>

