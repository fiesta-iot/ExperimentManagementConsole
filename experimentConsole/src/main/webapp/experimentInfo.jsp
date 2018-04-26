<%@ page language="java" contentType="text/html; charset=ISO-8859-1"  pageEncoding="ISO-8859-1"%> 
<%@page import="eu.fiesta_iot.platform.eee.console.CallEEEAPIs" %>
<%@page import="eu.fiesta_iot.platform.eee.console.Constants" %>
<!DOCTYPE html>
<%
	String token=request.getParameter("iPlanetDirectoryPro");
	String experimentID=request.getParameter("experimentID");	
%>

<html lang="en">
	<head>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="description" content="Introducing Lollipop, a sweet new take on website.">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0">
		<title>Experiment Management Console</title>

		<!-- Page styles -->
		<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:regular,bold,italic,thin,light,bolditalic,black,medium&amp;lang=en">
		<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
		<script src="https://code.getmdl.io/1.1.3/material.min.js"></script>
		<link rel="stylesheet" href="https://code.getmdl.io/1.1.3/material.min.css">
		<script  src="mdl-selectfield.min.js"></script>
        <link rel="stylesheet" href="mdl-selectfield.min.css">
		<link rel="stylesheet" href="styles.css">
		<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
		
		<style>
			#view-source {
				position: fixed;
				display: block;
				right: 0;
				bottom: 0;
				margin-right: 40px;
				margin-bottom: 40px;
				z-index: 900;
			}
			.line-title {
			    font-size: 20px;
			    margin-bottom: 10px;
			    padding-top: 1px; /* Allows for hr margin to start at top of h2 */
			}

			/* clearfix for floats */
			.line-title:after {
			    content: "";
			    display: table;
			    clear: both;
			}

			.line-title span {
			    padding-right: 10px;
			    float: left;
			}

			.line-title hr {
			    border:1px solid #DDD;
			    border-width: 1px 0 0 0;
			    margin-top: 11px;
			}
		</style>
	</head>
  	<body>
		<div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
			<div class="website-header mdl-layout__header mdl-layout__header--waterfall">
		    	<div class="mdl-layout__header-row">
		      		<span class="website-title mdl-layout-title mdl-typography--font-thin" style="color: gray">
		        		Experiment Management Console
		      		</span>
			    </div>
		  	</div>
			
		  	<div class="website-content mdl-layout__content">
		  	<%= CallEEEAPIs.UserExperimentInfo(experimentID,token) %>
			</div>
		</div>
	</body>
</html>
