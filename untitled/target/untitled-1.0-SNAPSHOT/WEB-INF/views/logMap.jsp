<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.utils.StatisticsUtils" %>
<%@ page import="fr.untitled2.entities.Log" %>
<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ page import="fr.untitled2.entities.Log" %>
<%@ page import="fr.untitled2.utils.StatisticsUtils" %>
<%@ page import="fr.untitled2.statistics.LogStatistics" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%
    Log log = (Log) request.getAttribute(MVCConstants.log_attribute);
%>
<html>
    <head>
        <link type="text/css" rel="stylesheet" href="/stylesheets/basic.css" />
        <script type="text/javascript" src="/resources/jquery/1.9.1/jquery.js"></script>
        <script type="text/javascript" src="/resources/jquery.ui.map.js"></script>
        <script type="text/javascript" src="/resources/jquery.ui.map.overlays.js"></script>
        <title>Log Map - <%=log.getName()%></title>
    </head>
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/mapMenu.jsp" /><%
if (log != null) {
        LogStatistics logStatistics = StatisticsUtils.getLogStatistics(log); %>
                <div class="item rounded dark">
                    <div id="map_canvas" class="map rounded"></div>
                </div>
        		<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBO3kuvX9XZB-Xsb5FwmWx398K3j488o9E&sensor=false"></script>
        		<script type="text/javascript" src="/resources/underscore-1.2.2/underscore.min.js"></script>
        		<script type="text/javascript" src="/resources/backbone-0.5.3/backbone.min.js"></script>
        		<script type="text/javascript" src="/resources/prettify/prettify.min.js"></script>

        		<script type="text/javascript">
                    $('#map_canvas').gmap().bind('init', function() {
                            $.getJSON( '/ihm/logs/images/json?logKey=<%=log.getInternalId()%>', function(data) {
                                $.each( data.markers, function(i, marker) {
                                    $('#map_canvas').gmap('addMarker', {
                                        'position': new google.maps.LatLng(marker.latitude, marker.longitude),
                                        'bounds': false
                                    }).click(function() {
                                        $('#map_canvas').gmap('openInfoWindow', { 'content': marker.title + '<br><img src=\"' + marker.lowResolutionImageUrl + '\"/>' }, this);
                                    });
                                });
                            });

                            $.getJSON( '/ihm/logs/json?logKey=<%=log.getInternalId()%>', function(data) {
                                var points = [];
		                        $.each( data.markers, function(i, marker) {
		                            points.push(new google.maps.LatLng(marker.latitude, marker.longitude));
            		            });
            		            var bounds = new google.maps.LatLngBounds();
                                for (var i = 0; i < points.length; i++) {
                                    bounds.extend(points[i]);
                                }
                                $('#map_canvas').gmap('get', 'map').fitBounds(bounds);
                                $('#map_canvas').gmap('addShape', 'Polyline', { 'strokeColor': "#FF0000", 'strokeOpacity': 0.8, 'strokeWeight': 2, 'path': points});
            	            });
                        });
                </script>
<% } %>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
