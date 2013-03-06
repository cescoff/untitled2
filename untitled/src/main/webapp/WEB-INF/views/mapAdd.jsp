<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="fr.untitled2.servlet.ServletConstants" %>
<%@ page import="fr.untitled2.entities.PictureMap" %>
<%@ page import="fr.untitled2.utils.UserUtils" %>
<%@ page import="fr.untitled2.entities.User" %>
<%@ page import="fr.untitled2.servlet.ServletConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.lang.Iterable" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    request.setAttribute("pageTitle", "Map Creation");
    User user = UserUtils.getUser();
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/mapMenu.jsp" />
                <p><messages:message packageName="fr.untitled2.bundle.views.mapAdd" key="welcomemessage" defaultValue="A map displays all images corresponding to an event. So a map has a start date and an end date. For example : My hollidays in Thailland from a date to another date"/></p>
                <div class="item rounded dark">
                    <div class="white" style="text-align: right;">
                        <form:form modelAttribute="<%=fr.untitled2.mvc.MVCConstants.map_form_attribute%>" method="POST" action="${pageContext.request.contextPath}/ihm/maps/create" style="margin: 0 255px 0;">
                            <table style="border: 0;">
                                <tr>
                                    <td style="border: 0;"><messages:message packageName="fr.untitled2.bundle.views.mapAdd" key="datestart" defaultValue="Date Start"/>: </td>
                                    <td style="border: 0;"><form:input type="text" path="dateStart" cssStyle="height: 25px;" /></td>
                                </tr>
                                <tr>
                                    <td style="border: 0;"><messages:message packageName="fr.untitled2.bundle.views.mapAdd" key="dateend" defaultValue="Date End"/>: </td>
                                    <td style="border: 0;"><form:input type="text" path="dateEnd" cssStyle="height: 25px;" /></td>
                                </tr>
                                <tr>
                                    <td style="border: 0;"><messages:message packageName="fr.untitled2.bundle.views.mapAdd" key="name" defaultValue="Name"/>: </td>
                                    <td style="border: 0;"><form:input type="text" path="name" cssStyle="height: 25px;" /></td>
                                </tr>
                                <tr>
                                    <td style="border: 0;" colspan="2" align="center"><input type="submit" value="<messages:message packageName="fr.untitled2.bundle.views.mapAdd" key="save" defaultValue="Save !!"/>" /></td>
                                </tr>
                            </table>
                        </form:form>
                    </div>
                </div>
            </div>
        </div>
        <script>
            $(function() {
                $( "#dateStart" ).datepicker({ dateFormat: "<%=user.getDateFormat().toLowerCase().replaceAll("yy", "y")%>" });
            });
            $(function() {
                $( "#dateEnd" ).datepicker({ dateFormat: "<%=user.getDateFormat().toLowerCase().replaceAll("yy", "y")%>" });
            });
        </script>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>