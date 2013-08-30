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
    request.setAttribute("pageTitle", "Log List");
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/logMenu.jsp" /><%
        LogList logList = (LogList) request.getAttribute(MVCConstants.log_list_attribute);
        if (logList != null) {
            List<Log> logs = logList.getLogs(); %>
                <table>
                    <tr>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.logList" key="logname" defaultValue="Log Name"/></td>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.logList" key="logend" defaultValue="Log end"/></td>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.logList" key="timezone" defaultValue="Time Zone"/></td>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.logList" key="logpoints" defaultValue="Log points"/></td>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.logList" key="distance" defaultValue="Distance"/></td>
                        <td class="caption"><img src="/images/icons/black/check_mark.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.views.logList" key="status" defaultValue="Status"/>"/></td>
                        <td class="caption"><img src="/images/icons/black/trash.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.views.logList" key="delete" defaultValue="Delete"/>"/></td>
                    </tr><%
            for (Log log: logs) {
                pageContext.setAttribute("logStartDate", log.getStartTime());
                pageContext.setAttribute("logEndDate", log.getEndTime()); %>
                    <tr>
                        <td><a href="/ihm/logs/map?logKey=<%=log.getInternalId()%>"><%=log.getName()%></a></td>
                        <td><userdate:userdatetimeformat value="logEndDate" /></td>
                        <td><%=log.getTimeZoneId()%></td>
                        <td><%=log.getPointCount()%> <messages:message packageName="fr.untitled2.bundle.views.logList" key="trackpoints" defaultValue="trackpoints"/></td>
                        <td><%=new Double(log.getDistance() / 1000).intValue()%> <messages:message packageName="fr.untitled2.bundle.views.logList" key="kilometer" defaultValue="km"/></td>
                        <td><%
            if (log.isValidated()) { %>
                            <img src="/images/icons/black/sunny.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.views.logList" key="valid" defaultValue="valid"/>"/><%
            } else { %>
                            <a href="/ihm/logs/validate-log?logKey=<%=log.getInternalId()%>"><img src="/images/icons/black/check_mark.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.views.logList" key="validate" defaultValue="validate"/>"/></a><%
            }%>
                        </td>
                        <td><a href="/ihm/logs/delete-log?logKey=<%=log.getInternalId()%>"><img src="/images/icons/black/trash.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.views.logList" key="delete" defaultValue="Delete"/>"/></a></td>
                    </tr><%
            } %>
                </table><%
            if (logList.getPageNumber() > 0) {%>
                <a href="/ihm/logs/list?pageNumber=<%=logList.getPageNumber() - 1%>"><img src="/images/icons/black/rewind.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.views.logList" key="previous" defaultValue="Previous"/>"/></a><%
            }
            if (logList.getPageNumber() < logList.getNextPageNumber()) { %>
                <a href="/ihm/logs/list?pageNumber=<%=logList.getNextPageNumber()%>"><img src="/images/icons/black/fast_forward.png" width="25" title="<messages:message packageName="fr.untitled2.bundle.views.logList" key="next" defaultValue="Next"/>"/></a><%
            }
        } else { %>
                <messages:message packageName="fr.untitled2.bundle.views.logList" key="nologmessage" defaultValue="Ach mein Gott !!! You haven't any logs at the moment, <a href='http://www.mypicturelog.com/start-using-it'>click here</a> to add some logs"/><%
        } %>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
