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

                        function displayBatchTaskList() {
                            data = '{"serverId":"<%=request.getParameter("serverId")%>"}'
                            $.ajax({
                                type: "POST",
                                url: "/api/server/getServerBatchTasks",
                                data: data,
                                success: function (json) {
                                    html = '<table><tr><td class="caption">Request ServerName</td><td class="caption">Start</td><td class="caption">End</td><td class="caption">Batchlet Name</td><td class="caption">Logs</td></tr>';

                                    for (index = 0; index < json.descriptions.length; index++) {
                                        html += '<tr>';
                                        html += '<td>' + json.descriptions[ index ].requestBatchServerName + '</td>';
                                        html += '<td>' + json.descriptions[ index ].startDate + '</td>';
                                        html += '<td>' + json.descriptions[ index ].endDate + '</td>';
                                        html += '<td>' + json.descriptions[ index ].batchletName + '</td>';
                                        html += '<td><a href="#" onClick="displayBatcletLogs(\'' + json.descriptions[ index ].batchTaskId + '\')">logs</a></td>';
                                        html += '</tr>';
                                    }
                                    html += '</table>';
                                    $("#serverList").html(html);
                                },
                                dataType: "json"
                            });
                        }

                        function displayBatcletLogs(batchTaskId) {
                            data = '{"batchTaskId":"' + batchTaskId + '"}'
                            $.ajax({
                                type: "POST",
                                url: "/api/server/getBatchTaskLogs",
                                data: data,
                                success: function (json) {
                                    html = '<a href="#" onClick="displayBatchTaskList()">Return to batch task list</a><br>';

                                    $("#serverList").html(html + json.message);
                                },
                                dataType: "json"
                            });
                        }

                        displayBatchTaskList();
                    </script>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
