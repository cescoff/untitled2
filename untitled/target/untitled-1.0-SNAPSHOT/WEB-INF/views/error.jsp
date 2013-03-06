<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<%
    request.setAttribute("pageTitle", "Home");
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
                <div class="homeMenuBlock">
                    <h1><messages:message packageName="fr.untitled2.bundle.views.error" key="message" defaultValue="Ooops !! We're sorry!! An error has occured"/></h1>
                </div>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>