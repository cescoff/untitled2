<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.business.beans.MapList" %>
<%@ page import="fr.untitled2.entities.PictureMap" %>
<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%@ taglib prefix="userdate" uri="/WEB-INF/dateTimeLib.tld"%>
<%
    request.setAttribute("pageTitle", "Map List");
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/mapMenu.jsp" /><%
        MapList mapList = (MapList) request.getAttribute(MVCConstants.map_list_attribute);
        if (mapList != null) {
            List<PictureMap> maps = mapList.getPictureMap();
        %>
                <table>
                    <tr>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.mapList" key="name" defaultValue="Name"/></td>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.mapList" key="datestart" defaultValue="Date Start"/></td>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.mapList" key="dateend" defaultValue="Date End"/></td>
                        <td class="caption"><messages:message packageName="fr.untitled2.bundle.views.mapList" key="sharingurl" defaultValue="Sharing Url"/></td>
                    </tr><%
            for (PictureMap map: maps) {
                pageContext.setAttribute("mapStartDate", map.getPeriodStart());
                pageContext.setAttribute("mapEndDate", map.getPeriodEnd()); %>
                    <tr>
                        <td><%=map.getName()%></td>
                        <td><userdate:userdatetimeformat value="mapStartDate" /></td>
                        <td><userdate:userdatetimeformat value="mapEndDate" /></td>
                        <td><a href="/ihm/maps/view?mapKey=<%=map.getSharingKey()%>">view/share</a></td>
                    </tr><%
            } %>
                </table><%
        }%>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>