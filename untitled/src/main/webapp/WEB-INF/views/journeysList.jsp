<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.utils.StatisticsUtils" %>
<%@ page import="fr.untitled2.entities.Log" %>
<%@ page import="fr.untitled2.business.beans.LogList" %>
<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ page import="fr.untitled2.entities.TrackPoint" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="userdate" uri="/WEB-INF/dateTimeLib.tld"%>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%
    request.setAttribute("pageTitle", "Journey List");
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                    <div id="serverList"></div>
                    <script>
                        data = '{"message":"<%=request.getRemoteAddr()%>"}'
                        $.ajax({
                            type: "POST",
                            url: "/api/server/getJourneysStatistics",
                            data: data,
                            success: function (json) {
                                html = '<table><tr><td class="caption">Date</td><td class="caption">Start</td><td class="caption">End</td><td class="caption">Duration</td><td class="caption">Distance</td><td class="caption">Avg Speed</td><td class="caption">Max Speed</td></tr>';

                                for (index = 0; index < json.statistics.length; index++) {
                                    html += '<tr>';
                                    html += '<td><a href="/ihm/servers/getJourneysStatistics?journeyId=' + json.statistics[ index ].journeyId + '">' + json.statistics[ index ].date + '</a></td>';
                                    html += '<td>' + json.statistics[ index ].start + '</td>';
                                    html += '<td>' + json.statistics[ index ].end + '</td>';
                                    html += '<td>' + json.statistics[ index ].duration + '</td>';
                                    html += '<td>' + json.statistics[ index ].distance + ' km</td>';
                                    html += '<td>' + json.statistics[ index ].averageSpeed + ' km/h</td>';
                                    html += '<td>' + json.statistics[ index ].maxSpeed + ' km/h</td>';
                                    html += '</tr>';
                                }
                                html += '</table>';
                                $("#serverList").append(html);
                            },
                            dataType: "json"
                        });
                    </script>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
