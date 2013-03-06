<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.utils.StatisticsUtils" %>
<%@ page import="fr.untitled2.entities.Log" %>
<%@ page import="fr.untitled2.business.beans.LogList" %>
<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ page import="fr.untitled2.entities.PictureMap" %>
<%@ page import="fr.untitled2.business.beans.MapMarkers" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%
    PictureMap pictureMap = (PictureMap) request.getAttribute(MVCConstants.map_attribute);
    boolean isHeadlessMode = "true".equals(request.getParameter("headless"));
%>
<html>
    <head>
        <link type="text/css" rel="stylesheet" href="/stylesheets/jqueryfileupload/jquery.fileupload-ui.css" />
        <link rel="stylesheet" href="http://blueimp.github.com/cdn/css/bootstrap.min.css">
        <link rel="stylesheet" href="http://blueimp.github.com/cdn/css/bootstrap-responsive.min.css">
        <link rel="stylesheet" type="text/css" href="/stylesheets/lightbox/jquery.lightbox-0.5.css" media="screen" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/basic.css" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/elastislide/elastislide.css" />
        <link type="text/css" rel="stylesheet" href="/stylesheets/elastislide/custom.css" />
        <script type="text/javascript" src="/resources/jquery/1.9.1/jquery.js"></script>
        <script type="text/javascript" src="/resources/jquery.ui.map.js"></script>
        <script type="text/javascript" src="/resources/jquery.ui.map.overlays.js"></script>
        <script type="text/javascript" src="/resources/elastislide/modernizr.custom.17475.js"></script>
        <script type="text/javascript" src="/resources/lightbox/jquery.lightbox-0.5.min.js"></script>

        <script type="text/javascript">
            var _gaq = _gaq || [];
            _gaq.push(['_setAccount', 'UA-38855868-1']);
            _gaq.push(['_trackPageview']);

            (function() {
                var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
                ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
                var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
            })();

        </script>
        <title>PictureMap - <%=pictureMap.getName()%></title>
    </head>
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container"><%
if (!isHeadlessMode) { %>
			    <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/mapMenu.jsp" /><%
}
if (pictureMap != null) {
    MapMarkers mapMarkers = (MapMarkers) request.getAttribute(MVCConstants.map_marker_attribute);
    if (mapMarkers != null && mapMarkers.getMarkers().size() > 0) {
        if (mapMarkers.isUserMapMarker() && !isHeadlessMode) {%>
                <script type="text/javascript">
                    function select_all(obj) {
                        var text_val=eval(obj);
                        text_val.focus();
                        text_val.select();
                        if (!document.all) return; // IE only
                        r = text_val.createTextRange();
                    }
                </script>
                <div id="sharingArea" class="item rounded dark" style="display:none;">
                    <div class="white">
                        <messages:message packageName="fr.untitled2.bundle.views.map" key="urllabel" defaultValue="Share (copy URL to clipboard)"/> : <input size="105" style="width: 610px; height: 25px;" value="http://application.mypicturelog/ihm/maps/view?mapKey=<%=request.getParameter("mapKey")%>&headless=true"
                                onclick="select_all(this)" name="url" type="text"/>
                        <a href="#" onClick="$('#sharingArea').css('display', 'none'); $('#sharingLink').css('display', 'block');"><messages:message packageName="fr.untitled2.bundle.views.map" key="hidesharingurl" defaultValue="Hide sharing url"/></a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="#" onClick="$('#banner').css('display', 'none'); $('#subMenu').css('display', 'none'); $('#sharingArea').css('display', 'none'); $('#sharingLink').css('display', 'none'); $('#undoDisplaySharingMode').css('display', 'block');"><messages:message packageName="fr.untitled2.bundle.views.map" key="displaysharingmode" defaultValue="Display sharing mode"/></a>
                    </div>
                </div><%
            if (!isHeadlessMode) { %>
                <div id="sharingLink">
                    <a href="#" onClick="$('#sharingArea').css('display', 'block'); $('#sharingLink').css('display', 'none'); $('#banner').css('display', 'block'); $('#subMenu').css('display', 'block');"><messages:message packageName="fr.untitled2.bundle.views.map" key="showsharingurl" defaultValue="Show sharing url"/></a>
                </div>
                <div id="undoDisplaySharingMode" style="display:none;">
                    <a href="#" onClick="$('#sharingArea').css('display', 'none'); $('#sharingLink').css('display', 'block'); $('#banner').css('display', 'block'); $('#subMenu').css('display', 'block'); $('#undoDisplaySharingMode').css('display', 'none');"><messages:message packageName="fr.untitled2.bundle.views.map" key="displaynormalmode" defaultValue="Display normal mode"/></a>
                </div><%
            }
        } else { %>
                <!-- MapMarkers does not belong to user --><%
        } %>
                <div class="item rounded dark">
                    <div id="map_canvas" class="map rounded"></div>
                </div>
                <div id="imageLinks">
                    <ul id="carousel" class="elastislide-list"><%
        for (int index = 0; index < Math.min(mapMarkers.getMarkers().size(), 10); index++) { %>
                        <li><a href="#" onClick="showMarker(<%=index%>)"><img src="<%=mapMarkers.getMarkers().get(index).getSquareLowResolutionImageUrl()%>" width="75"/></a></li><%
        } %>
                    </ul>
                </div>
                <div id="lightBoxImages" style="display:none">
                </div>
                <script>
                    var lightBoxUrls = [];<%
        for (int index = 0; index < Math.min(mapMarkers.getMarkers().size(), 10); index++) { %>
                    lightBoxUrls.push('<%=mapMarkers.getMarkers().get(index).getHighResolutionImageUrl()%>');<%
        } %>
                </script>
        		<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyBO3kuvX9XZB-Xsb5FwmWx398K3j488o9E&sensor=false"></script>
        		<script type="text/javascript" src="/resources/underscore-1.2.2/underscore.min.js"></script>
        		<script type="text/javascript" src="/resources/backbone-0.5.3/backbone.min.js"></script>
        		<script type="text/javascript" src="/resources/prettify/prettify.min.js"></script>
        		<!--script type="text/javascript" src="js/demo.js"></script-->
        		<script type="text/javascript" src="/resources/elastislide/jquerypp.custom.js"></script>
        		<script type="text/javascript" src="/resources/elastislide/jquery.elastislide.js"></script>
        		<script src="http://tympanus.net/codrops/adpacks/csscustom.js"></script>
        		<script type="text/javascript">
        		    var infoWindows = [];
                    var mapMarkers = [];
                    var points = [];
                    var carousel = $('#carousel').elastislide();
                    var map = $('#map_canvas').gmap('get', 'map');<%
        for (int index = 0; index < Math.min(mapMarkers.getMarkers().size(), 10); index++) { %>
                    points.push(new google.maps.LatLng(<%=mapMarkers.getMarkers().get(index).getLatitude()%>, <%=mapMarkers.getMarkers().get(index).getLongitude()%>));

                    // Affichage des info bulles
                    var infowindow<%=index%> = new google.maps.InfoWindow({
                        content: '<%=mapMarkers.getMarkers().get(index).getTitle()%><br><a rel="lightbox<%=index%>" href="<%=mapMarkers.getMarkers().get(index).getHighResolutionImageUrl()%>"><img src="<%=mapMarkers.getMarkers().get(index).getLowResolutionImageUrl()%>" /></a>',
                    });
                    var marker<%=index%> = new google.maps.Marker({
                        position: new google.maps.LatLng(<%=mapMarkers.getMarkers().get(index).getLatitude()%>, <%=mapMarkers.getMarkers().get(index).getLongitude()%>),
                        map: map,
                        title: '<%=mapMarkers.getMarkers().get(index).getTitle()%>'
                    });

                    google.maps.event.addListener(marker<%=index%>, 'click', function() {
                        infowindow<%=index%>.open(map,marker<%=index%>);
                    });
                    infoWindows.push(infowindow<%=index%>);
                    mapMarkers.push(marker<%=index%>);<%
        } %>
                    // On set une premiere fois le centre de la map
                    var bounds = new google.maps.LatLngBounds();
                    for (var i = 0; i < points.length; i++) {
                        bounds.extend(points[i]);
                    }
                    $('#map_canvas').gmap('get', 'map').fitBounds(bounds);

                    $('#map_canvas').gmap().bind('init', function() {
                        $.getJSON( '/ihm/maps/json?mapKey=<%=pictureMap.getSharingKey()%>', function(data) {

                            $.each( data.markers, function(i, marker) {
                                // Points pour la PolyLine
                                points.push(new google.maps.LatLng(marker.latitude, marker.longitude));

                                // Liens pour afficher le carousel de miniatures
                                if (i > 9) {
                                    $('#carousel').append('<li><a href="#" onClick="showMarker(' + i + ')"><img src="' + marker.squareLowResolutionImageUrl + '" width="75"/></a></li>');
                                    carousel.add();
                                    lightBoxUrls.push(marker.highResolutionImageUrl);
                                    // Affichage des info bulles
                                    var infowindow = new google.maps.InfoWindow({
                                        content: marker.title + '<br><a rel="lightbox' + i + '" href="' + marker.highResolutionImageUrl + '"><img src="' + marker.lowResolutionImageUrl + '" /></a>',
                                    });
                                    var map = $('#map_canvas').gmap('get', 'map');
                                    var marker = new google.maps.Marker({
                                        position: new google.maps.LatLng(marker.latitude, marker.longitude),
                                        map: map,
                                        title: marker.title
                                    });

                                    google.maps.event.addListener(marker, 'click', function() {
                                        $('#lightBoxImages').empty();
                                        infowindow.open(map,marker);
                                        for (var index = 0; index < lightBoxUrls.length; index++) {
                                            if (index > i) {
                                                $('#lightBoxImages').append('<a rel="lightbox' + i + '" href="' + lightBoxUrls[index] + '">Image ' + index + '</a>');
                                            }
                                        }
                                        $('a[rel^="lightbox"]').lightBox();
                                    });
                                    infoWindows.push(infowindow);
                                    mapMarkers.push(marker);
                                 }

                            });

                            $('#map_canvas').gmap('addShape', 'Polyline', { 'strokeColor': "#FF0000", 'strokeOpacity': 0.8, 'strokeWeight': 2, 'path': points});
                            bounds = new google.maps.LatLngBounds();
                            for (var i = 0; i < points.length; i++) {
                                bounds.extend(points[i]);
                            }
                            $('#map_canvas').gmap('get', 'map').fitBounds(bounds);
                        });
                    });

                    function clickOnLightBoxLink(index) {
                    }

                    function showMarker(index) {
                        $('#lightBoxImages').empty();
                        var map = $('#map_canvas').gmap('get', 'map');
                        for (var i = 0; i < infoWindows.length; i++) {
                            infoWindows[i].close();
                        }
                        infoWindows[index].open(map,mapMarkers[index]);
                        for (var i = 0; i < lightBoxUrls.length; i++) {
                            if (i > index) {
                                $('#lightBoxImages').append('<a rel="lightbox' + i + '" href="' + lightBoxUrls[i] + '">Image ' + i + '</a>');
                            }
                        }
                        $('a[rel^="lightbox"]').lightBox();
                        return false;
                    };
                </script><%
    }
} %>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
