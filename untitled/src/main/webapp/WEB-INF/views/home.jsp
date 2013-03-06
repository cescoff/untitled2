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
			<div id="container">
                <div class="homeMenuBlock">
                    <h1><messages:message packageName="fr.untitled2.bundle.views.home" key="title" defaultValue="Here are actions you can perform"/></h1>
                    <ul class="homeMenu">
                        <li class="homeMenuItem"><h2><a class="homeMenuItemLink" href="/ihm/logs"><messages:message packageName="fr.untitled2.bundle.views.home" key="logs" defaultValue="Logs"/></a> : <messages:message packageName="fr.untitled2.bundle.views.home" key="logs_caption" defaultValue="manage / view your logs"/></h2></li>
                        <li class="homeMenuItem"><h2><a class="homeMenuItemLink" href="/ihm/images"><messages:message packageName="fr.untitled2.bundle.views.home" key="images" defaultValue="Images"/></a> : <messages:message packageName="fr.untitled2.bundle.views.home" key="images_caption" defaultValue="manage / view your images"/></h2></li>
                        <li class="homeMenuItem"><h2><a class="homeMenuItemLink" href="/ihm/maps"><messages:message packageName="fr.untitled2.bundle.views.home" key="maps" defaultValue="Maps"/></a> : <messages:message packageName="fr.untitled2.bundle.views.home" key="maps_caption" defaultValue="manage / view your shared maps"/></h2></li>
                    </ul>
                    <h2><messages:message packageName="fr.untitled2.bundle.views.home" key="help" defaultValue="Need help, want to know how it works : <a href='https://sites.google.com/a/mypicturelog.com/howto'>help</a>"/></h2>
                </div>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>