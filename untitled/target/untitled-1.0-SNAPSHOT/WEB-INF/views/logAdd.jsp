<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.servlet.ServletConstants" %>
<%@ page import="fr.untitled2.entities.Image" %>
<%@ page import="fr.untitled2.servlet.ServletConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.lang.Iterable" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${!ajaxRequest}">
<%
    request.setAttribute("pageTitle", "Add Log");
%>
<html>
            <jsp:include page="template/head.jsp" />
<body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/logMenu.jsp" />
</c:if>
                <div id="fileuploadContent">
                    <h2><messages:message packageName="fr.untitled2.bundle.views.logAdd" key="title" defaultValue="Log Upload"/></h2>
                    <form id="fileuploadForm" action="add" method="POST" enctype="multipart/form-data" class="cleanform">
                            <div class="header">
                                <c:if test="${not empty message}">
                                    <div id="message" class="success">${message}</div>
                                </c:if>
                            </div>
                            <p><select name="fileType">
                                <option><messages:message packageName="fr.untitled2.bundle.views.logAdd" key="choosefiletype" defaultValue="Choose file type"/></option>
                                <option value="mpl">My Picture Log</option>
                                <option value="gpx">GPX</option>
                                <option value="kml">KML</option>
                             </select></p>
                            <label for="file"><messages:message packageName="fr.untitled2.bundle.views.logAdd" key="file" defaultValue="File"/></label>
                            <input id="file" type="file" name="file" />
                            <p><button type="submit"><messages:message packageName="fr.untitled2.bundle.views.logAdd" key="upload" defaultValue="Upload"/></button></p>
                    </form>
                    <script type="text/javascript">
                        $(document).ready(function() {
                            $('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
                            $("#fileuploadForm").ajaxForm({ success: function(html) {
                                    $("#fileuploadContent").replaceWith(html);
                                }
                            });
                        });
                    </script>
                </div>
<c:if test="${!ajaxRequest}">
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>
</c:if>