<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%@ taglib uri="http://www.springframework.org/spring-social/facebook/tags" prefix="facebook" %>
<html>
<%
    request.setAttribute("pageTitle", "Login");
%>
<html>
            <jsp:include page="template/head.jsp" />
    <body>
        <facebook:init appId="433841930037842" />
		<div id="page">
			<div id="container">
                <div class="homeMenuBlock">
                    <h1><messages:message packageName="fr.untitled2.bundle.views.start" key="welcome" defaultValue="Look !!! It is so simple to use MyPictureLog"/></h1>
                    <h2><messages:message packageName="fr.untitled2.bundle.views.start" key="title" defaultValue="You choose the way you log in"/></h2>
                    <ul><%
    if (request.getAttribute(MVCConstants.google_login_attribute) != null) {%>
                        <li><h2><messages:message packageName="fr.untitled2.bundle.views.start" key="googleaccount" defaultValue="Google Account"/> : <a href="<%=request.getAttribute(MVCConstants.google_login_attribute)%>"><img src="/images/googleplus.png" width="34"/></a></h2></li><%
    }
    if (request.getAttribute(MVCConstants.facebook_login_attribute) != null) {%>
                        <li>
                            <h2>
                                <messages:message packageName="fr.untitled2.bundle.views.start" key="facebookaccount" defaultValue="Facebook Account"/> :
                                <form id="fb_signin" action="/ihm/signin/facebook" method="POST">
                                    <div id="fb-root"></div>
                                    <input type="hidden" name="returnPath" value="<%=request.getParameter("returnPath")%>"/>
                                    <fb:login-button onlogin="$('#fb_signin').submit();" v="2" length="long" perms="email"/>
                                </form>
                            </h2>
                        </li><%
    } else { %>
                        <li><h2><messages:message packageName="fr.untitled2.bundle.views.start" key="facebookaccountsoon" defaultValue="Facebook Account (coming soon)"/> : <a href="#"><img src="/images/facebook.png" width="34"/></a></h2></li><%
    } %>
                    </ul>
                </div>
            </div>
        </div>
        <jsp:include page="template/footer.jsp" />
    </body>
</html>