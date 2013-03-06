<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.servlet.ServletConstants" %>
<%@ page import="fr.untitled2.business.beans.LightWeightImage" %>
<%@ page import="fr.untitled2.business.beans.ImageList" %>
<%@ page import="fr.untitled2.mvc.MVCConstants" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="fr.untitled2.utils.UserUtils" %>
<%@ page import="fr.untitled2.entities.User" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="userdate" uri="/WEB-INF/dateTimeLib.tld"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%
    request.setAttribute("pageTitle", "Image List");
    User user = UserUtils.getUser();
%>
<html>
    <jsp:include page="template/head.jsp" />
    <body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
			    <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/imageMenu.jsp" /><%
        ImageList imageList = (ImageList) request.getAttribute(MVCConstants.image_list_attribute);
        if (imageList != null) {
            List<LightWeightImage> images = imageList.getImages(); %>
				<!-- Start Advanced Gallery Html Containers -->
				<div id="showFilter">
				    <a href="#" onClick="$('#filter').css('display', 'block'); $('#showFilter').css('display', 'none');"><messages:message packageName="fr.untitled2.bundle.views.imageList" key="showfilter" defaultValue="Show filter"/></a>
				</div>
				<div id="filter" class="item rounded dark" style="display:none">
                    <div class="white">
                        <form:form modelAttribute="<%=fr.untitled2.mvc.MVCConstants.image_form_attribute%>" method="POST" action="${pageContext.request.contextPath}/ihm/images/list">
                            <messages:message packageName="fr.untitled2.bundle.views.imageList" key="filter.datestart" defaultValue="Date Start"/> : <form:input type="text" path="dateStart" cssStyle="height: 25px;" />&nbsp;&nbsp;<messages:message packageName="fr.untitled2.bundle.views.imageList" key="filter.dateend" defaultValue="Date End"/> : <form:input type="text" path="dateEnd" cssStyle="height: 25px;" /><input type="submit" value="<messages:message packageName="fr.untitled2.bundle.views.imageList" key="filter.button" defaultValue="Filter now!!"/>" />
                        </form:form>
                        <a href="#" onClick="$('#filter').css('display', 'none'); $('#showFilter').css('display', 'block');"><messages:message packageName="fr.untitled2.bundle.views.imageList" key="hidefilter" defaultValue="Hide filter"/></a>
                    </div>
				</div>
				<script>

				</script>
                <div id="gallery" class="content">
                    <div id="controls" class="controls"></div>
                    <div class="slideshow-container">
                        <div id="loading" class="loader"></div>
                        <div id="slideshow" class="slideshow"></div>
                    </div>
                    <div id="caption" class="caption-container"></div>
                </div>
                <div id="thumbs" class="navigation">
                    <ul class="thumbs noscript"><%
            for (LightWeightImage image: images) {
                pageContext.setAttribute("imgDate", image.getDateTaken());
                String geoCode = "";
                if (image.getLatitude() != null && image.getLongitude() != null) {
                    geoCode = "[" + image.getLatitude() + ", " + image.getLongitude() + "]";
                }
                String titleGeoCode = "";
                if (StringUtils.isNotEmpty(geoCode)) {
                    titleGeoCode = " - " + geoCode;
                }%>
                        <li>
                            <a class="thumb" name="optionalCustomIdentifier" href="<%=image.getMediumResolutionImageUrl()%>" title="<%=titleGeoCode%>">
                                <img src="<%=image.getSquareLowResolutionImageUrl()%>" alt="<%=titleGeoCode%>" width="75" />
                            </a>
                            <div class="caption">
                                <div class="download">
                                    <a href="<%=image.getOriginalImageUrl()%>"><messages:message packageName="fr.untitled2.bundle.views.imageList" key="downloadoriginal" defaultValue="Download Original"/></a>
                                </div>
                                <div class="image-title"><messages:message packageName="fr.untitled2.bundle.views.imageList" key="datetaken" defaultValue="Date taken"/> : <userdate:userdatetimeformat value="imgDate" /></div>
                                <div class="image-desc"><%
                if (StringUtils.isNotEmpty(geoCode)) { %>
                                    <messages:message packageName="fr.untitled2.bundle.views.imageList" key="geocode" defaultValue="Geocode"/> : <%=geoCode%><%
                } else { %>
                                    <messages:message packageName="fr.untitled2.bundle.views.imageList" key="nogeocodemessage" defaultValue="No Geolocalisation informations"/><%
                } %>
                                </div>
                            </div>
                        </li><%
            } %>
                    </ul>
                </div>
                <div style="clear: both;"></div>
                <script type="text/javascript">
                    jQuery(document).ready(function($) {
                        // We only want these styles applied when javascript is enabled
                        $('div.navigation').css({'width' : '300px', 'float' : 'left'});
                        $('div.content').css('display', 'block');

                         // Initially set opacity on thumbs and add
                        // additional styling for hover effect on thumbs
                        var onMouseOutOpacity = 0.67;
                        $('#thumbs ul.thumbs li').opacityrollover({
                            mouseOutOpacity:   onMouseOutOpacity,
                            mouseOverOpacity:  1.0,
                            fadeSpeed:         'fast',
                            exemptionSelector: '.selected'
                        });

                        // Initialize Advanced Galleriffic Gallery
                        var gallery = $('#thumbs').galleriffic({
                            delay:                     2500,
                            numThumbs:                 15,
                            preloadAhead:              15,
                            enableTopPager:            true,
                            enableBottomPager:         true,
                            maxPagesToShow:            7,
                            imageContainerSel:         '#slideshow',
                            controlsContainerSel:      '#controls',
                            captionContainerSel:       '#caption',
                            loadingContainerSel:       '#loading',
                            renderSSControls:          true,
                            renderNavControls:         true,
                            playLinkText:              '<messages:message packageName="fr.untitled2.bundle.views.imageList" key="playslideshow" defaultValue="Play Slideshow"/>',
                            pauseLinkText:             '<messages:message packageName="fr.untitled2.bundle.views.imageList" key="pauseslideshow" defaultValue="Pause Slideshow"/>',
                            prevLinkText:              '<messages:message packageName="fr.untitled2.bundle.views.imageList" key="previousphoto" defaultValue="&lsaquo; Previous Photo"/>',
                            nextLinkText:              '<messages:message packageName="fr.untitled2.bundle.views.imageList" key="nextphoto" defaultValue="Next Photo &rsaquo;"/>',
                            nextPageLinkText:          '<messages:message packageName="fr.untitled2.bundle.views.imageList" key="next" defaultValue="Next &rsaquo;"/>',
                            prevPageLinkText:          '<messages:message packageName="fr.untitled2.bundle.views.imageList" key="prev" defaultValue="&lsaquo; Prev"/>',
                            enableHistory:             false,
                            autoStart:                 false,
                            syncTransitions:           true,
                            defaultTransitionDuration: 900,
                            onSlideChange:             function(prevIndex, nextIndex) {
                                // 'this' refers to the gallery, which is an extension of $('#thumbs')
                                this.find('ul.thumbs').children()
                                 .eq(prevIndex).fadeTo('fast', onMouseOutOpacity).end()
                                 .eq(nextIndex).fadeTo('fast', 1.0);
                            },
                            onPageTransitionOut:       function(callback) {
                                this.fadeTo('fast', 0.0, callback);
                            },
                            onPageTransitionIn:        function() {
                                this.fadeTo('fast', 1.0);
                            }
                        });
                    });
                </script><%
        } else { %>
        <messages:message packageName="fr.untitled2.bundle.views.imageList" key="noimagemessage" defaultValue="Ach mein Gott !!! You haven't any image at the moment, <a href='/ihm/images/add'>click here</a> to add some images"/><%
        } %>
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