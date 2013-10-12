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
    request.setAttribute("pageTitle", "Server List");
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
                        data = '{"hostIpAddress":"<%=request.getRemoteAddr()%>"}'
                        $.ajax({
                            type: "POST",
                            url: "/api/server/serverList",
                            data: data,
                            success: function (json) {
                                html = '<table><tr><td class="caption">ServerName</td><td class="caption">CPU Cores</td><td class="caption">Online</td><td class="caption">Connected</td><td class="caption">Uptime</td></tr>';

                                for (index = 0; index < json.servers.length; index++) {
                                    html += '<tr>';
                                    html += '<td>' + json.servers[ index ].serverName + '</td>';
                                    html += '<td>' + json.servers[ index ].cpuCoreCount + ' cores</td>';
                                    html += '<td>' + json.servers[ index ].onLine + '</td>';
                                    html += '<td>' + json.servers[ index ].connected + '</td>';
                                    html += '<td>' + json.servers[ index ].uptime + '</td>';
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
