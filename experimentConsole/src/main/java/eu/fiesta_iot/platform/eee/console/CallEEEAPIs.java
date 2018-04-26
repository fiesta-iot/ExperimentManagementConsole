/*
===========================================================
Experiment Management Console
Copyright (C) 2018  Authors: Rachit Agarwal.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

contact: rachit.agarwal@inria.fr 
===========================================================
*/
package eu.fiesta_iot.platform.eee.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiesta_iot.platform.eee.console.Constants;

import eu.fiestaiot.commons.expdescriptiveids.model.ExpDescriptiveIDs;
import eu.fiestaiot.commons.expdescriptiveids.model.FemoDescriptiveID;
import eu.fiestaiot.commons.expdescriptiveids.model.FismoDescriptiveID;
import eu.fiestaiot.commons.fedspec.model.FISMO;

public class CallEEEAPIs {
	static Logger log = LoggerFactory.getLogger(CallEEEAPIs.class);
	public static String UserExperimentstoString(String token){
		String finalNavigation="";
		
		String userID=getUserID(token);

		if (!userID.isEmpty()){	
			try{
				ExpDescriptiveIDs expDescriptiveID=getConnection(URLEncoder.encode(userID, "UTF-8"),token);
				String idgetId="";
				for (FemoDescriptiveID id: expDescriptiveID.getFemoDescriptiveID()){
					idgetId=id.getId();
					String idgetName=id.getName();
				
					finalNavigation+=
					"<tr>"
						+ "<td style=\"text-align: left\">"+idgetId+"</td>\n"
						+ "<td style=\"text-align: left\">"+idgetName+"</td>\n"
						+ "<td>\n"
							+ "<form id=\""+idgetId+"\" action=\""+Constants.getPath()+"/experimentInfo.jsp\" class=\"mdl-grid\">"
								+ "<input type=\"hidden\" name=\"experimentID\" value=\""+idgetId+"\"/>"
								+ "<input type=\"hidden\" name=\"iPlanetDirectoryPro\" value=\""+token+"\"/>"
								+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" id=\"b_"+idgetId+"\" style=\"width: 100pt\" onclick='emc2load();'>Select</button>"
							+ "</form>"
							+ "<br>\n"
						+ "</td>\n"
					+ "</tr>\n"
					+ "<script type=\"text/javascript\">"
						+ "function emc2load() { "
							+"var form = document.getElementById(\""+idgetId+"\");"
					    	+"form.submit();"
						+ "}"
					+ "</script>\n";
				}
			}catch(Exception e){
				log.debug("No FEDSPECs associated to userID: "+userID);
				e.printStackTrace();
			}
		}
		return finalNavigation;
	}
	
	public static String UserExperimentInfo(String femoID, String token){

		String userID=getUserID(token);

		List<String> fismoIDsAttached= new ArrayList<String>();
		String finalNavigation="";
		if (!userID.isEmpty()){
			try{
				ExpDescriptiveIDs expDescriptiveID=getConnection(URLEncoder.encode(userID, "UTF-8"),token);
				for (FemoDescriptiveID id: expDescriptiveID.getFemoDescriptiveID()){
					if (id.getId().equals(femoID)){
						finalNavigation+= firstPart(id,userID,fismoIDsAttached,token);
						finalNavigation+= secondPart(id,fismoIDsAttached,token);
						finalNavigation+= thirdPart(femoID,fismoIDsAttached,token);
					}
				}	
			}catch(Exception e){
				log.debug("No FEDSPECs associated to userID: "+userID);
				e.printStackTrace();
			}
		}
		return finalNavigation;
	}
	
	private static String firstPart(FemoDescriptiveID id,String userID, List<String> fismoIDsAttached,String token){
		String doiFull="";
		
		for (int i=0;i<id.getDomainOfInterest().size();i++){
			doiFull+="<option>"+id.getDomainOfInterest().get(i).replaceAll("http://purl.org/iot/vocab/m3-lite#", "")+"</option>";
		}
		
		String firstPart=
		"<!-- First  Part-->"
		+ "<span class=\"mdl-typography--font-light mdl-typography--subhead\">"
			+ "<h4 class=\"mdl-card__title-text line-title\"><span>Experiment Details</span><hr /></h4>"
			+ "<table class=\"mdl-data-table mdl-js-data-table mdl-shadow--2dp\">"
				+ "<tbody>"
					+ "<tr>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">Experiment ID and Name:</td>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">"+id.getId()+"</td>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">"+id.getName()+"</td>"
					+ "</tr>"
					+ "<tr>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">Experiment Description: </td>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">"
							+ "<div class=\"mdl-textfield mdl-js-textfield\">"
								+ "<textarea class=\"mdl-textfield__input\" type=\"text\" id=\"experimentdesc\" disabled>"+id.getDescription()+"</textarea>"
							+ "</div>"
						+ "</td>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\"></td>"
					+ "</tr>"
					+ "<tr>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">Experiment Domain of Interest List: </td>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">"
							+"<div class=\"mdl-selectfield mdl-js-selectfield mdl-selectfield--floating-label\" style=\"width:180px\">"
								+	"<select id=\"profile_information_form_dob_2\" name=\"profile_information_form[dob(2i)]\" class=\"mdl-selectfield__select\">"
									+doiFull					
								+ "</select>"
								+ "<label for=\"profile_information_form_dob_2\" class=\"mdl-selectfield__label\">List</label>"
							+ "</div>"	 
						+ "</td>"
						+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\"></td>"
					+ "</tr>"
					+"<tr >"
						+ "<td style=\"text-align: center\" class=\"mdl-data-table__cell--non-numeric\" colspan=\"3\">"
							+"You can download results of past executions of FEMO/FISMOs using "+Constants.getDownloadLink()+" "
							+"for more details see training material."
						+ "</td>"
					+"</tr>"
				+ "</tbody>"
			+ "</table>"
		+ "</span>"
		+ "<br><br>";
		return firstPart;
	}
	
	private static String secondPart(FemoDescriptiveID id,List<String> fismoIDsAttached, String token){
		String secondPart="<!-- Second  Part-->"
		+ "<span class=\"mdl-typography--font-light mdl-typography--subhead\">"
			+ "<h4 class=\"mdl-card__title-text line-title\"><span>Associated FISMOs</span><hr /></h4>"
			+ "<table class=\"mdl-data-table mdl-js-data-table mdl-shadow--2dp\">"
				+ "<thead>"
					+ "<tr>"
						+ "<th style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">JobID</th>"
						+ "<th style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">FISMO Name</th>"
						+ "<th style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">Ownership</th>"
						+ "<th style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">Status</th>"
						+ "<th style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">Stopped/Started</th>"
						+ "<th></th>"
						+ "<th></th>"
						+ "<th></th>"
					+ "</tr>"
				+ "</thead>"
			+ "<tbody>";
			int count=0;
			
			if (id.getFismoDescriptiveID().size()==0){
				log.error("no FISMOs for the experiment ID "+ id.getId()+" by the provided by the userID: "+getUserID(token) );
			}
			else{
				for (FismoDescriptiveID fdi: id.getFismoDescriptiveID()){
					fismoIDsAttached.add(fdi.getId());
					count++;
					secondPart+= associatedFISMOOwner(fdi, id.getId(),count,token);
				}
			}
			
			JSONArray subsArray=new JSONArray();
			try{
				JSONObject callParameters=new JSONObject();
				callParameters.put("femoID",URLEncoder.encode(id.getId(), "UTF-8"));
				JSONObject subscriptions=new JSONObject(getConnection(Constants.getExperimenterSubscriptions(), callParameters,"query",token));
				if (subscriptions.has("Subscriptions")){
					subsArray=subscriptions.getJSONArray("Subscriptions");
					secondPart+= associatedFISMOSubscriber(id, count, subsArray,fismoIDsAttached,token);
				}
			}catch(Exception e){
				log.error("no Subscriptions for the experiment ID "+ id.getId()+" by the made by the userID: "+getUserID(token));
			}
			secondPart+="<tr>"
						+ "<td class=\"mdl-data-table__cell--non-numeric\" colspan=7 height=700>"
							+ "<div id=\"dialogLogsContent\"></div>"
						+ "</td>"
					+ "</tr>";
			secondPart+="</tbody>"
			+ "</table>"
		+ "</span>";
		return secondPart;
	}

	private static String associatedFISMOOwner(FismoDescriptiveID id,String femoID, int count,String token) {
		String partSecondPart="";
		JSONObject job=getJobStatus(id.getId(),femoID,true,token);
		log.debug("json job object associatedFISMOOwner:"+job);
		partSecondPart+= "<tr>";
		if (job!=null){
			partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
					+"<label style=\"color:#FF0000 \">"+job.getString("JobID")+"</label>"
				+ "</td>";
		}
		else{
			log.debug("json job object associatedFISMOOwner is here because of null");
			partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
					+"<label style=\"color:#FF0000 \">No jobID yet</label>"
				+ "</td>";
		}
			
		partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
			+"<label style=\"color:#FF0000 \" id=\"experimentdesc"+count+"\">"+id.getName()+"</label>"
			+"<div id=\"experimentSnackBardesc"+count+"\" class=\"mdl-js-snackbar mdl-snackbar\">"
			  +"<div class=\"mdl-snackbar__text\"></div>"
			  +"<button class=\"mdl-snackbar__action\" type=\"button\"></button>"
			+"</div>"
			+"<script>"
				+"(function() {"
				  +"'use strict';"
				  +"window['counter'] = 0;"
				  +"var snackbarContainer = document.querySelector('#experimentSnackBardesc"+count+"');"
				  +"var showToastButton = document.querySelector('#experimentdesc"+count+"');"
				  +"showToastButton.addEventListener('click', function() {"
				    +"'use strict';"
				    +"var data = {message: '"+id.getDescription()+"'};"
				    +"snackbarContainer.MaterialSnackbar.showSnackbar(data);"
				  +"});"
				+"}());"
			+"</script>"
		
		+ "</td>"
		+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\"><label style=\"color:#F5A9A9\">Owner</label></td>";
			
		String logs="";
		String state="";
		String jobID="";
		if(job!=null){
			if (job.has("state")){
				partSecondPart+= "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\"><label style=\"color:#FF0000 \">"+job.getString("state")+"</label></td>";
				state=job.getString("state");
				jobID=job.getString("JobID");
				logs=getlog(job.getString("JobID"),token);
			}
			else {
				partSecondPart+= "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\"><label style=\"color:#FF0000 \">NOT YET SCHEDULED</label></td>";
				//logs="<option value=\"\" disabled></option>";
				logs="{"
						+ "\\\"cols\\\": ["
		    			+ "{\\\"label\\\":\\\"Execution ID\\\",\\\"type\\\":\\\"number\\\"},"
						+ "{\\\"label\\\":\\\"Execution Time\\\",\\\"type\\\":\\\"number\\\"},"
						+ "{\\\"label\\\":\\\"Length Of Data\\\",\\\"type\\\":\\\"number\\\"}"
					+ "],"
					+ "\"rows\": ["
					+"{\\\"c\\\":["
			    		+ "{\\\"v\\\":0},"
			    		+ "{\\\"v\\\":0},"
						+ "{\\\"v\\\":0}]"
			      + "}"
					+ "]}";
			}
		}else{
			partSecondPart+= "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">"
					+ "<label style=\"color:#FF0000 \">NOT YET SCHEDULED</label>"
				+ "</td>";
			//logs="<option value=\"\" disabled></option>";
			logs="{"
					+ "\\\"cols\\\": ["
	    			+ "{\\\"label\\\":\\\"Execution ID\\\",\\\"type\\\":\\\"number\\\"},"
					+ "{\\\"label\\\":\\\"Execution Time\\\",\\\"type\\\":\\\"number\\\"},"
					+ "{\\\"label\\\":\\\"Length Of Data\\\",\\\"type\\\":\\\"number\\\"}"
				+ "],"
				+ "\"rows\": ["
				+"{\\\"c\\\":["
		    		+ "{\\\"v\\\":0},"
		    		+ "{\\\"v\\\":0},"
					+ "{\\\"v\\\":0}]"
		      + "}"
				+ "]}";
		}
		partSecondPart+=toggleFunctionality(count,state,id.getId(),femoID,jobID,token);
		
		partSecondPart+="<td class=\"mdl-data-table__cell--non-numeric\">"
				+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" id=\"bttnViewLogs"+count+"\" style=\"width: 100pt\" onclick=\"viewLogs"+count+"();\">View Logs</button>"
				+"<script type=\"text/javascript\">"
					+ viewLogs(logs,count)
				+ "</script>\n"
			+"</td>";
		partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
			+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" id=\"bpoll_"+id.getId()+"\" style=\"width: 100pt\" onclick='return ePollNow"+count+"()'>Poll Now</button>"
			+ "<script type=\"text/javascript\">"
				+ePoll(id.getId(), femoID,  count,token,true)
			+ "</script>\n"
		+ "</td>";
		if(job!=null){
			if (job.has("state")){
				partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
				+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" style=\"width: 100pt\" onclick=\"eDeleteFISMO"+count+"();\">"
				+ "Delete Job</button><br>"
				+ "<script type=\"text/javascript\">"
					+deleteFISMO(jobID,count,token)
				+ "</script>"
				+ "</td>";
			}
			else{
				partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\"></td>";
			}
		}
		else{
			partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\"></td>";
		}
		partSecondPart+= "</tr>";
		return partSecondPart;
	}
	
	private static String associatedFISMOSubscriber(FemoDescriptiveID id, int count, JSONArray subscribed,List<String> fismoIDsAttached, String token) {
		String partSecondPart="";
		for (int i=0;i<subscribed.length();i++){
			JSONObject fismoANDJOB=subscribed.getJSONObject(i);
			FISMO fismo=getFismoDescription(fismoANDJOB.getString("fismoID"),token);
			count++;
			String logs="";
			JSONObject job=getJobStatus(fismoANDJOB.getString("fismoID"),id.getId(),false,token);
			
			partSecondPart+= "<tr>";
			
			if (job!=null){
				partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
						+"<label style=\"color:#FF0000 \">"+job.getString("JobID")+"</label>"
					+ "</td>";
			}
			else{
				partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
						+"<label style=\"color:#FF0000 \">No jobID yet</label>"
					+ "</td>";
			}

			partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
			//+ "<textarea class=\"mdl-textfield__input\" type=\"text\" rows= \"3\" id=\"experimentdesc\" disabled>"+fismo.getId()+":"+fismo.getDescription()+"</textarea></td>"
					+"<label style=\"color:#FF0000 \" id=\"experimentdesc"+count+"\">"+fismo.getName()+"</label>"
							+"<div id=\"experimentSnackBardesc"+count+"\" class=\"mdl-js-snackbar mdl-snackbar\">"
							  +"<div class=\"mdl-snackbar__text\"></div>"
							  +"<button class=\"mdl-snackbar__action\" type=\"button\"></button>"
							+"</div>"
							+"<script>"
								+"(function() {"
								  +"'use strict';"
								  +"window['counter'] = 0;"
								  +"var snackbarContainer = document.querySelector('#experimentSnackBardesc"+count+"');"
								  +"var showToastButton = document.querySelector('#experimentdesc"+count+"');"
								  +"showToastButton.addEventListener('click', function() {"
								    +"'use strict';"
								    +"var data = {message: '"+id.getDescription()+"'};"
								    +"snackbarContainer.MaterialSnackbar.showSnackbar(data);"
								  +"});"
								+"}());"
							+"</script>"
					//+ "<div class=\"mdl-tooltip\" for=\"experimentdesc"+count+"\">"+fismo.getDescription()+"</div>"
			+ "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\"><label style=\"color:#F7D358 \">Subscribed</label></td>";
			
			fismoIDsAttached.add(fismoANDJOB.getString("fismoID"));

			
			String state="";
			String jobID="";
			if(job!=null){
				if (job.has("state")){
					partSecondPart+= "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\"><label style=\"color:#FF0000 \">"+job.getString("state")+"</label></td>";
					state=job.getString("state");
					jobID=job.getString("JobID");
					logs=getlog(job.getString("JobID"),token);
				}
				else {
					partSecondPart+= "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">"
							+ "<label style=\"color:#FF0000 \">NOT YET SCHEDULED</label></td>";
					//logs="<option value=\"\" disabled></option>";
					logs="{"
							+ "\\\"cols\\\": ["
			    			+ "{\\\"label\\\":\\\"Execution ID\\\",\\\"type\\\":\\\"number\\\"},"
							+ "{\\\"label\\\":\\\"Execution Time\\\",\\\"type\\\":\\\"number\\\"},"
							+ "{\\\"label\\\":\\\"Length Of Data\\\",\\\"type\\\":\\\"number\\\"}"
						+ "],"
						+ "\"rows\": ["
						+"{\\\"c\\\":["
				    		+ "{\\\"v\\\":0},"
				    		+ "{\\\"v\\\":0},"
							+ "{\\\"v\\\":0}]"
				      + "}"
						+ "]}";
				}
			}else{
				partSecondPart+= "<td style=\"text-align: left\" class=\"mdl-data-table__cell--non-numeric\">"
						+ "<label style=\"color:#FF0000 \">NOT YET SCHEDULED</label></td>";
				logs="{"
						+ "\\\"cols\\\": ["
		    			+ "{\\\"label\\\":\\\"Execution ID\\\",\\\"type\\\":\\\"number\\\"},"
						+ "{\\\"label\\\":\\\"Execution Time\\\",\\\"type\\\":\\\"number\\\"},"
						+ "{\\\"label\\\":\\\"Length Of Data\\\",\\\"type\\\":\\\"number\\\"}"
					+ "],"
				+ "\"rows\": ["
				+"{\\\"c\\\":["
		    		+ "{\\\"v\\\":0},"
		    		+ "{\\\"v\\\":0},"
					+ "{\\\"v\\\":0}]"
		      + "}"
				+ "]}";
			}

			partSecondPart+=toggleFunctionality(count,state,fismoANDJOB.getString("fismoID"),id.getId(),jobID,token);
			

			partSecondPart+="<td class=\"mdl-data-table__cell--non-numeric\">\n"
					+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" id=\"bttnViewLogs"+count+"\" style=\"width: 100pt\" onclick=\"viewLogs"+count+"();\">View Logs</button>\n"
					+"<script type=\"text/javascript\">\n"
						+ viewLogs(logs,count)
					+ "</script>\n"
				+"</td>";
			partSecondPart+= "<td class=\"mdl-data-table__cell--non-numeric\">"
				+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" style=\"width: 100pt\" onclick=\"ePollNow"+count+"();\">"
				+ "Poll Now</button><br>"
				+ "<script type=\"text/javascript\">"
					+ePoll(fismoANDJOB.getString("fismoID"), id.getId(), count,token,false)
				+ "</script>"
			+ "</td>"
			+ "<td class=\"mdl-data-table__cell--non-numeric\">"
				+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" style=\"width: 100pt\" onclick=\"eUnsubscribeFISMO"+count+"();\">"
				+ "Unsubscribe</button><br>"
				+ "<script type=\"text/javascript\">"
					+unsubscribeFISMO(fismoANDJOB.getString("fismoID"), id.getId(),count,token)
				+ "</script>"
			+ "</td>"
			+ "</tr>";
		}
		return partSecondPart;
	}

	private static String viewLogs(String logs,int count){
		String function= "function viewLogs"+count+"() { \n"
				+ "google.charts.load('visualization', '1.0', {\n"
					+ "'packages' : [ 'line', 'corechart' ]"
				+ "});\n"
				+" google.charts.setOnLoadCallback(drawDashboard); \n"
			    
				+ "function drawDashboard() {\n"
					+ "var myObject = eval('( "+ logs + " )');\n"
					+ "var data = new google.visualization.DataTable(myObject);\n"
					+ "var options = {\n"
					+ 	"chart: {\n"
							+ "title: 'Log Details',\n"
							+"legend: { position: 'bottom' }\n"
						+ "},\n"
						+ "height: 700,"
						+ "series: {"
							+ "0: {axis: 'ExecutionTime'},\n"
							+ "1: {axis: 'LengthOfData'}\n"
						+ "},\n"
						+ "axes: {\n"
							+ "y: {\n"
								+ "ExecutionTime: {label: 'Execution Time in millisec'},\n"
								+ "LengthOfData: {label: 'Length Of Data in bytes'}\n"
							+ "}\n"
						+ "}\n"
					+ "};\n"
					+ "var chart = new google.charts.Line(document.getElementById('dialogLogsContent'));\n"
					+ "chart.draw(data, options);\n"
				+ "}\n"
		+ "}";
		return function;
	}
	private static String toggleFunctionality(int count, String state,String id, String femoID,String jobID,String token) {
		String checked="checked";
		if(state.equals("PAUSED") || state.equals("COMPLETED") || state.isEmpty()){
			checked="";
		}
		FISMO fismo=getFismoDescription(id,token);
		String part= "<td>"
			+ "<label class=\"mdl-switch mdl-js-switch mdl-js-ripple-effect\" for=\"switch"+count+"\">"
			+ "<input type=\"checkbox\" id=\"switch"+count+"\" class=\"mdl-switch__input\""+checked+" onclick='doAction"+count+"()'>"
					+ "<span class=\"mdl-switch__label\"></span>"
				+ "</label>"
			+ "</td>"
			+ "<script type=\"text/javascript\">"	
				+doAction(jobID, count,token)
				+scheduleFISMO( fismo, count,  id, femoID,  token)
				+restartFISMO(jobID, count,token)
				+stopFISMO(jobID,count,token)
			+ "</script>\n";
		return part;
	}
	
	private static FISMO getFismoDescription(String fismoID,String token){
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(Constants.getERMServiceModelPath());
        target = target.queryParam("fismoID", fismoID);
        Response response = target.request().header("iPlanetDirectoryPro", token).get();
        if (response.getStatus() == HttpURLConnection.HTTP_OK){
        	FISMO fismo = response.readEntity(FISMO.class);
        	response.close(); 
        	client.close();
			return fismo;
		}
       response.close();
       client.close();
       return null;
	}

	private static JSONObject getJobStatus(String fismoID, String femoID, boolean owner,String token) {
		try{
			JSONObject callParameters=new JSONObject();
			callParameters.put("fismoID",fismoID);
			callParameters.put("femoID",femoID);
			log.debug(callParameters.toString());
			JSONObject responsefromEEE=new JSONObject(getConnectionJobs(Constants.getJobIDFismoUserFemoGiven(),callParameters,owner,"header",token));
			
			if (responsefromEEE.has("jobID")){
				JSONObject callParametersOther=new JSONObject();
				callParametersOther.put("jobID",URLEncoder.encode(responsefromEEE.getString("jobID"), "UTF-8"));
				JSONObject responsefromEEEjobstatus=new JSONObject(getConnection(Constants.getJobStatus(),callParametersOther,"query",token));
				if (responsefromEEEjobstatus.has("JobID") && responsefromEEEjobstatus.has("state")){
					if (responsefromEEEjobstatus.getString("JobID").equals(responsefromEEE.getString("jobID"))){
						return responsefromEEEjobstatus;
					}
				}
			}
			else{
				return null;
			} 
		}catch(Exception e){
			log.error("either fismoID:" +fismoID+", femoID"+ femoID+" or token not correct. Could be connection Issues as well. ",e);
		}
		return null;
	}
	
	private static String getlog(String jobID,String token) {
		String convertedCharts ="";
		try{
			JSONObject callParameters=new JSONObject();
			callParameters.put("jobID",URLEncoder.encode(jobID, "UTF-8"));
			JSONObject responsefromEEE=new JSONObject(getConnection(Constants.getJobIDLog(),callParameters,"query",token));
			if (responsefromEEE.has("ExecutionLog")){
				JSONArray arr=responsefromEEE.getJSONArray("ExecutionLog");
				convertedCharts=convertToCharts(arr);
			}
		}catch(Exception e){
			log.error("either jobID:" +jobID+" or token not correct. Could be connection Issues as well.",e);
		}
		return convertedCharts;
	}
	private static String convertToCharts(JSONArray jsonArray){
		int index=0;
		int len=jsonArray.length();
		String converted="{"
    			+ "\\\"cols\\\": ["
    			+ "{\\\"label\\\":\\\"Execution ID\\\",\\\"type\\\":\\\"number\\\"},"
				+ "{\\\"label\\\":\\\"Execution Time\\\",\\\"type\\\":\\\"number\\\"},"
				+ "{\\\"label\\\":\\\"Length Of Data\\\",\\\"type\\\":\\\"number\\\"}"
			+ "],"
		+ "\\\"rows\\\": [";
		for(int n = 0; n < len; n++){
		    JSONObject object = jsonArray.getJSONObject(n);
		    index++;
		    converted+="{\\\"c\\\":["
		    		+ "{\\\"v\\\":"+index+"},"
		    		+ "{\\\"v\\\":"+object.getLong("executionTime")+"},"
					+ "{\\\"v\\\":"+object.getString("dataConsumed")+"}]"
		      + "}";
		    if (index<len){
		    	converted+=", ";
        	}
		}
		if (len==0){
			converted+="{\\\"c\\\":["
		    		+ "{\\\"v\\\":0},"
		    		+ "{\\\"v\\\":0},"
					+ "{\\\"v\\\":0}]"
		      + "}";
		}
		converted+= "]"
        		+ "}";
		return converted;
	}
	private static String thirdPart(String femoID,List<String> fismoIDsAttached,String token){
		List<FISMO> allfismo=null;
		try{
			allfismo=getConnectionDiscoverableFISMO(token);
		}catch(Exception e){log.error("Token not correct. Could be connection Issues as well.",e);}
		String dropdownthirdPart="";
		int count=0;
		String selected="selected";
		for 	(FISMO fi: allfismo){
			if (!fismoIDsAttached.contains(fi.getId())){
				if (count==0) 
					dropdownthirdPart+=	"<option value=\""+fi.getId()+"\""+selected+">"+fi.getName()+": "+fi.getDescription()+"</option>";
				else
					dropdownthirdPart+=	"<option value=\""+fi.getId()+"\">"+fi.getName()+": "+fi.getDescription()+"</option>";
					
				count++;
			}
		}
		String disabled="";
		if (count==0){
			disabled="disabled";
			dropdownthirdPart="<option value=\"\"></option>";
		}
		
		String thirdPart="<!-- Third Part-->"
		//+ "<form id=\"my_form\" action=\"#\" class=\"mdl-grid\">"
		+ "<br><br><span class=\"mdl-typography--font-light mdl-typography--subhead\">"
			+ "<h4 class=\"mdl-card__title-text line-title\"><span>Other available FISMO ID for Subscription</span><hr /></h4>"
			+ "<div class=\"mdl-color--whitemdl-shadow--2dp mdl-cell mdl-cell--12-col mdl-grid\">"
			
			+ "<table class=\"mdl-data-table mdl-js-data-table mdl-shadow--2dp\">"
				+ "<tbody >"
					+ "<tr>"
						+ "<td class=\"mdl-data-table__cell--non-numeric\">"
							+ "<div class=\"mdl-selectfield mdl-js-selectfield mdl-selectfield--floating-label\" style=\"width:600px\">"
								+ " <select id=\"selSubscribe\" name=\"selSubscribe\" class=\"mdl-selectfield__select\" "+disabled+">"
									+dropdownthirdPart
								+ "</select>"
								+ "<label for=\"selSubscribe\" class=\"mdl-selectfield__label\" style=\"width:600px\">Available FISMOs (choose one)</label>"
							+"</div>"
						+"</td>"
						+"<td class=\"mdl-data-table__cell--non-numeric\">"
							+ "<div>To Send data to:"
								+ "<div class=\"mdl-textfield mdl-js-textfield mdl-textfield--floating-label\">"
								+ "<input class=\"mdl-textfield__input\" type=\"text\" id=\"SubscribeFISMO\" "+disabled+" "
										+ "type=\"url\" required"
										+ " onkeyup=\"toggleButton('bttnsubsc');\">"
										+"<script type=\"text/javascript\">\n"
											+"function toggleButton(bttnID){\n"
											// referred https://mathiasbynens.be/demo/url-regex and https://gist.github.com/dperini/729294
											+"var re=new RegExp(/^(?:(?:https?|ftp):\\/\\/)?(?:\\S+(?::\\S*)?@)?(?:(?!(?:10|127)(?:\\.\\d{1,3}){3})(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]-*)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))\\.?)(?::\\d{2,5})?(?:[/?#]\\S*)?$/i);"
											+ " var text=document.getElementById(\"SubscribeFISMO\").value;\n"
											 + "if(document.getElementById(\"SubscribeFISMO\").value != '') {\n"
											 	+ "if (re.test(text)) {\n"
											 	+ " document.getElementById(bttnID).removeAttribute(\"disabled\");\n"
											+"}\n"
											+ "else {document.getElementById(bttnID).setAttribute(\"disabled\", \"disabled\");}}\n"
											+ "else {document.getElementById(bttnID).setAttribute(\"disabled\", \"disabled\");}}\n"
									+ "</script>\n"
								+ "<label class=\"mdl-textfield__label\" for=\"SubscribeFISMO\">url</label>"
								+ "</div>"
							+ "</div>"
						+"</td>"
					+"</tr>"
					+"<tr>"
						+"<td class=\"mdl-data-table__cell--non-numeric\">"
							+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" id=\"bttnViewSelected\" style=\"width: 100pt\" onclick=\"viewSelected();\">View Selected</button>"
							+"<script type=\"text/javascript\">"
								+ viewFISMO(token)
							+ "</script>\n"
							+"<dialog class=\"mdl-dialog\" id=\"dialogView\" style=\"width:800px;\">"
						    	+"<h4 class=\"mdl-dialog__title\">FISMO</h4>"
						    	+"<div class=\"mdl-dialog__content\" id=\"dialogContent\" style=\"overflow:auto;\">"
				    				+"<p>"
				    				+"</p>"
				    			+"</div>"
				    			+"<div class=\"mdl-dialog__actions\">"
									+"<button type=\"button\" class=\"mdl-button close\">close</button>"
								+"</div>"
							+"</dialog>"
						+"</td>"
						+"<td class=\"mdl-data-table__cell--non-numeric\">"
							+ "<button class=\"mdl-button mdl-js-button mdl-button--raised mdl-button--colored\" id=\"bttnsubsc\" style=\"width: 100pt\" onclick=\"subscribe();\" disabled>Subscribe</button>"
							+"<script type=\"text/javascript\">"
								+ subscribeFISMO(femoID,token)
							+ "</script>\n"
						+"</td>"
					+"</tr>"
				+"</tbody>"	
			+"</table>"
		+ "</span>";
		//+ "</form>"
		return thirdPart;
	}
	
	private static String doAction(String jobID, int count,String token){
		String function="function doAction"+count+"() { "
				+ " var xhr = new XMLHttpRequest();\n"
				+ " var url = \""+Constants.getJobStatus()+"?jobID="+jobID+"\";\n"
				+ " var state=\"\";"
				+ " xhr.open(\"GET\", url, true);\n"
				+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
				+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
		    	+ " xhr.onreadystatechange = function() {\n"
		    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
						+"var data=xhr.responseText;\n"
						+"var jsonResponse = JSON.parse(data);"
						+"status=jsonResponse[\"state\"];"
						+"if(jsonResponse.hasOwnProperty('state')){\n"
							+"if (status==\"PAUSED\"|| status==\"COMPLETE\"){\n"
								+ "restartFISMO"+count+"()"
							+"}\n"
							+ "else if (status==\"NORMAL\"){\n"
								+"stopFISMO"+count+"()"
							+ "}"
							+ "else {\n"
								+"scheduleFISMO"+count+"()"
							+ "}"
						+ "}\n"
		    		+"} else if (xhr.readyState == 4) {\n"
		    			+"scheduleFISMO"+count+"()"
					+"}\n"
		    	+"}\n"
		    	+"xhr.send(null);\n"
			+ "}";
		return function;
	}
	
	private static String scheduleFISMO(FISMO fismo,int count, String id,String femoID, String token){
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		log.debug("timestamp"+ fismo.getExperimentControl().getScheduling().getStartTime().getTime());
		log.debug("stoptimestamp"+ fismo.getExperimentControl().getScheduling().getStopTime().getTime());
		String startdate=formatter.format(fismo.getExperimentControl().getScheduling().getStartTime().getTime());
		String stopdate=formatter.format(fismo.getExperimentControl().getScheduling().getStopTime().getTime());
						
		String payload="{\\\"startTime\\\":\\\""+startdate+"\\\", "
			+"\\\"stopTime\\\":\\\""+stopdate+"\\\", "
			+"\\\"periodicity\\\":"+fismo.getExperimentControl().getScheduling().getPeriodicity()+"}";
		String function="function scheduleFISMO"+count+"() { "
			+ " var xhr = new XMLHttpRequest();\n"
			+ " var url = \""+Constants.getScheduler()+"\";\n"
			+ " xhr.open(\"POST\", url, true);\n"
			+ " xhr.setRequestHeader(\"fismoID\",\""+id+"\");\n"
			+ " xhr.setRequestHeader(\"femoID\",\""+femoID+"\");\n"
			+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
			+ " xhr.setRequestHeader('timeSchedulePayload',\""+payload+"\");\n"
			+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
	    	+ " xhr.onreadystatechange = function() {\n"
	    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
					+"alert(\"Successfully Scheduled\");"
					+"document.location.reload();"
	    		+"} else if (xhr.readyState == 4) {\n"
				   +" alert(\"error text: \" + xhr.responseText);\n"
				+"}\n"
	    	+"}\n"
	    	+"xhr.send(null);\n"
		+ "}";
		return function;
	}
	
	private static String restartFISMO(String jobID, int count,String token){
		String function="function restartFISMO"+count+"() { "
			+ " var xhr = new XMLHttpRequest();\n"
			+ " var url = \""+Constants.getRestart()+"\";\n"
			+ " xhr.open(\"POST\", url, true);\n"
			+ " xhr.setRequestHeader('jobID',\""+jobID+"\");"
			+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
			+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
	    	+ " xhr.onreadystatechange = function() {\n"
	    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
				    +"alert(\"Successfully Restarted\");"
				    +"document.location.reload();"
	    		+"} else if (xhr.readyState == 4) {\n"
				   +" alert(\"error text: \" + xhr.responseText);\n"
				+"}\n"
	    	+"}\n"
	    	+"xhr.send(null);\n"
    	+ "}";
		return function;
	}
	private static String stopFISMO(String jobID,int count,String token){
		String function="function stopFISMO"+count+"() { "
			+ " var xhr = new XMLHttpRequest();\n"
			+ " var url = \""+Constants.getStop()+"\";\n"
			+ " xhr.open(\"POST\", url, true);\n"
			+ " xhr.setRequestHeader('jobID',\""+jobID+"\");"
			+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
			+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
	    	+ " xhr.onreadystatechange = function() {\n"
	    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
				    +"alert(\"Successfully Paused\");"
				    +"document.location.reload();"
	    		+"} else if (xhr.readyState == 4) {\n"
				   +" alert(\"error text: \" + xhr.responseText);\n"
				+"}\n"
	    	+"}\n"
	    	+"xhr.send(null);\n"
    	+ "}" ;
		return function;
	}
	private static String viewFISMO(String token){
		String function= "function viewSelected() { "
			+ " var fID = document.getElementById(\"selSubscribe\").value;"
			+ " var xhr = new XMLHttpRequest();\n"
			+ " var url = \""+Constants.getERMServiceModelPath()+"?fismoID=\"+fID;\n"
			+ " xhr.open(\"GET\", url, true);\n"
			+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
			+ " xhr.setRequestHeader('Content-Type', 'application/xml');\n"
	    	+ " xhr.onreadystatechange = function() {\n"
	    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
				    +"document.getElementById('dialogContent').textContent = new XMLSerializer().serializeToString(xhr.responseXML.documentElement); \n"
					+"var showViewDialogButtonModal = document.querySelector('#bttnViewSelected');\n"
					+"var dialogView = document.querySelector('#dialogView');\n"
					+"if (! dialogView.showModal) {\n"
					+"dialogPolyfill.registerDialog(dialogView);\n"
					+"}\n"
					+"var showClickHandler = function(event) {\n"
					+"dialogView.showModal();\n"
					+"};\n"
					+"showViewDialogButtonModal.addEventListener('click', showClickHandler);\n"
					+"dialogView.querySelector('.close').addEventListener('click', function() {\n"
					+" dialogView.close();\n"
					+"});\n"
	    		+"} else if (xhr.readyState == 4) {\n"
				   +" alert(\"error text: \" + xhr.responseText);\n"
				+"}\n"
	    	+"}\n"
	    	+"xhr.send(null);\n"
		+ "}";
		return function;
	}
	
	
	private static String subscribeFISMO(String femoID, String token){
		String function= "function subscribe() { "
			+ " var fID = document.getElementById(\"selSubscribe\").value;"
			+ " var expout = document.getElementById(\"SubscribeFISMO\").value;"
			+ " var expoutTEMP = '{\"url\":\"' +expout +'\"}';"
			+ " var xhr = new XMLHttpRequest();\n"
			+ " var url = \""+Constants.getSubscriptions()+"\";\n"
			+ " xhr.open(\"POST\", url, true);\n"
			+ " xhr.setRequestHeader(\"fismoID\",fID);\n"
			+ " xhr.setRequestHeader(\"femoID\",\""+femoID+"\");\n"
			+ " xhr.setRequestHeader('experimentOutput',expoutTEMP);\n"
			+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
			+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
	    	+ " xhr.onreadystatechange = function() {\n"
	    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
				    +"alert(\"successfully subscribed\");"
				    +"document.location.reload();"
	    		+"} else if (xhr.readyState == 4) {\n"
				   +" alert(\"error text: \" + xhr.responseText);\n"
				+"}\n"
	    	+"}\n"
	    	+"xhr.send(null);\n"
		+ "}";
		return function;
	}
	
	private static String unsubscribeFISMO(String fismoID, String femoID,int count, String token) {
		String function= "function eUnsubscribeFISMO"+count+"() { "
			+ "var parameters = \"?fismoID="+fismoID.toString()+"&femoID="+femoID+"\";"
			+ " var xhr = new XMLHttpRequest();\n"
			+ " var url = \""+Constants.getUnSubscriptions()+"?fismoID="+fismoID.toString()+"&femoID="+femoID+"\";"
			+ " xhr.open(\"POST\", url, true);\n"
			+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
			+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
	    	+ " xhr.onreadystatechange = function() {\n"
	    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
				    +"alert(\"successfully unsubscribed\");"
				    +"document.location.reload();"
	    		+"} else if (xhr.readyState == 4) {\n"
				   +" alert(\"error text: \" + xhr.responseText);\n"
				+"}\n"
	    	+"}\n"
	    	+"xhr.send(null);\n"
		+ "}";
		return function;
	}

	private static String deleteFISMO(String jobID, int count, String token) {
		String function= "function eDeleteFISMO"+count+"() { "
			+ " var xhr = new XMLHttpRequest();\n"
			+ " var url = \""+Constants.getEEEDeleteFISMO()+"\";"
			+ " xhr.open(\"POST\", url, true);\n"
			+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
			+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
			+ " xhr.setRequestHeader('jobID',\""+ jobID+"\");"
	    	+ " xhr.onreadystatechange = function() {\n"
	    	+ "	if(xhr.readyState == 4 && xhr.status == 200) {\n"
				    +"alert(\"successfully deleted\");"
				    +"document.location.reload();"
	    		+"} else if (xhr.readyState == 4) {\n"
				   +" alert(\"error text: \" + xhr.responseText);\n"
				+"}\n"
	    	+"}\n"
	    	+"xhr.send(null);\n"
		+ "}";
		return function;
	}
	
	private static String ePoll(String fismoID,String femoID, int count,String token,boolean owner) {
		String function= "function ePollNow"+count+"() {\n"
				+ " var xhr = new XMLHttpRequest();\n"
				+ " var url = \""+Constants.getPolling()+"?owner="+owner+"\";\n"
				+ " xhr.open(\"POST\", url, true);\n"
				+ " xhr.setRequestHeader('fismoID',\""+fismoID+"\");"
				+ " xhr.setRequestHeader('femoID',\""+femoID+"\");"
				+ " xhr.setRequestHeader('Content-Type', 'application/json');\n"
				+ " xhr.setRequestHeader('iPlanetDirectoryPro',\""+ token+"\");"
		    	+" xhr.onreadystatechange = function() {\n"
		    	+"	if(xhr.readyState == 4 && xhr.status == 200) {\n"
					    +"alert(\"Successfully polled, please keep the jobID for future ref.\"+ xhr.responseText);"
					 +"} else if (xhr.readyState == 4 && xhr.status == 504) {\n"
						   +" alert(\" Data cannot be sent to the URL you provided. Data is saved is ERS. \");\n"
					+"} else if (xhr.readyState == 4) {\n"
					   +" alert(\"error text: \" + xhr.responseText);\n"
					+"}\n"
		    	+"}\n"
		    	+"xhr.send(null);\n"
			+"}\n";
		return function;
	}
	
	private static String getContent(InputStream input) {
		 StringBuilder sb = new StringBuilder();
		 byte [] b = new byte[1024];
		 int readBytes = 0;
		 try {
			while ((readBytes = input.read(b)) >= 0) {
				 sb.append(new String(b, 0, readBytes, "UTF-8"));
			 }
			input.close();
			return sb.toString().trim();
		} catch (IOException e) {
			log.error("IOException",e);//e.printStackTrace();
			if (input != null)
				try {
					input.close();
				} catch (IOException e1) {
					log.error("IOException",e1);//e1.printStackTrace();
				}
		}
		 return null;
	 }

	private static ExpDescriptiveIDs getConnection(String userID,String token) throws Exception{
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(Constants.getERMAllUserExperimentsDescription());
        target=target.queryParam("userID", userID);
        Response response = target.request().header("iPlanetDirectoryPro", token).get();
        if (response.getStatus() == HttpURLConnection.HTTP_OK){
        	ExpDescriptiveIDs expDescriptiveIDs = response.readEntity(ExpDescriptiveIDs.class);
        	response.close(); 
        	client.close();
			return expDescriptiveIDs;
		}
       response.close();
       client.close();
       return null;
	}

	private static List<FISMO> getConnectionDiscoverableFISMO(String token) throws Exception{
	
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(Constants.getERMDiscoverableFismoPath());
		List<FISMO> fismo = target.request().header("iPlanetDirectoryPro", token).get(new GenericType<List<FISMO>>(){});
		client.close();
        return fismo;
	}
	
	private static String getConnectionJobs(String urlStr, JSONObject jsonobject,boolean owner,String where,String token) throws Exception{
		String parameters="?owner="+owner;
		if (where.equals("query")){
			//parameters="?owner="+owner;
			int length=jsonobject.keySet().size();
			int counter=1;
			for(Object key: jsonobject.keySet()){
				parameters+= key.toString()+"="+ jsonobject.getString(key.toString());
				if (counter<length){
					parameters+="&";
					counter++;
				}
			}
		}
				
		URL url = new  URL(urlStr+parameters);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		if (where.equals("header")){
			for(Object key: jsonobject.keySet()){
				conn.setRequestProperty (key.toString(), jsonobject.getString(key.toString()));
			}
		}
		conn.setRequestProperty ("iPlanetDirectoryPro", token);
		int response = conn.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK){
			return getContent(conn.getInputStream());
		}
		else return "{}";
	}
	
	private static String getConnection(String urlStr, JSONObject jsonobject,String where,String token) throws Exception{
		String parameters="";
		if (where.equals("query")){
			parameters="?";
			int length=jsonobject.keySet().size();
			int counter=1;
			for(Object key: jsonobject.keySet()){
				parameters+= key.toString()+"="+ jsonobject.getString(key.toString());
				if (counter<length){
					parameters+="&";
					counter++;
				}
			}
		}
				
		URL url = new  URL(urlStr+parameters);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		if (where.equals("header")){
			for(Object key: jsonobject.keySet()){
				conn.setRequestProperty (key.toString(), jsonobject.getString(key.toString()));
			}
		}
		conn.setRequestProperty ("iPlanetDirectoryPro", token);
		int response = conn.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK){
			String ret=	getContent(conn.getInputStream());
			return ret;
		}
		else return "{}";
	}
	
	public static String getUserID(String token){
		String userID="";

       try{
			URL url = new URL(Constants.getSecurityUserAPI());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST"); 
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("iPlanetDirectoryPro", token);

			OutputStreamWriter wr= new OutputStreamWriter(conn.getOutputStream());
			wr.flush();
			wr.close();
			int responseMC = conn.getResponseCode();
			log.debug("in getuserID: "+responseMC);
			if (responseMC == HttpURLConnection.HTTP_OK){
				String security = getContent(conn.getInputStream());
	        	JSONObject jObject=new JSONObject(security);
	        	if (jObject.has("id")){
	        		userID=jObject.getString("id");
	        	}
			}
		}catch(IOException e){
			log.error("IOException",e);//e.printStackTrace();
		}catch(Exception e){
			log.error("Exception",e);//e.printStackTrace();
		}
		return userID;
	}
}