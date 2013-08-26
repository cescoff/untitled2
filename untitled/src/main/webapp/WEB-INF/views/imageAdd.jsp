<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="fr.untitled2.servlet.ServletConstants" %>
<%@ page import="fr.untitled2.entities.Image" %>
<%@ page import="fr.untitled2.servlet.ServletConstants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.lang.Iterable" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="messages" uri="/WEB-INF/i18nLib.tld"%>
<%
    request.setAttribute("pageTitle", "Add Image");
%>
<html>
<jsp:include page="template/head.jsp" />
<body>
		<div id="page">
            <jsp:include page="template/topMenu.jsp" />
			<div id="container">
                <jsp:include page="template/banner.jsp" />
                <jsp:include page="template/imageMenu.jsp" />
                <p><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="welcomemessage" defaultValue="You can drag and drop your images"/></p>
                <form id="fileupload" method="post" enctype="multipart/form-data">
                    <div class="row fileupload-buttonbar">
                        <div class="span7">
                            <span class="btn btn-success fileinput-button">
                                <i class="icon-plus icon-white"></i>
                                <span><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="addfilesbutton" defaultValue="Add files..."/></span>
                                <input type="file" name="files" multiple>
                            </span>
                            <button type="submit" class="btn btn-primary start">
                                <i class="icon-upload icon-white"></i>
                                <span><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="startuploadbutton" defaultValue="Start upload"/></span>
                            </button>
                            <button type="reset" class="btn btn-warning cancel">
                                <i class="icon-ban-circle icon-white"></i>
                                <span><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="canceluploadbutton" defaultValue="Cancel upload"/></span>
                            </button>
                            <button type="button" class="btn btn-danger delete">
                                <i class="icon-trash icon-white"></i>
                                <span><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="deletebutton" defaultValue="Delete"/></span>
                            </button>
                            <input type="checkbox" class="toggle">
                        </div>
                        <!-- The global progress information -->
                        <div class="span5 fileupload-progress fade">
                            <!-- The global progress bar -->
                            <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                                <div class="bar" style="width:0%;"></div>
                            </div>
                            <!-- The extended global progress information -->
                            <div class="progress-extended">&nbsp;</div>
                        </div>
                    </div>
                    <!-- The loading indicator is shown during file processing -->
                    <div class="fileupload-loading"></div>
                    <br>
                    <!-- The table listing the files available for upload/download -->
                    <table role="presentation" class="table table-striped"><tbody class="files" data-toggle="modal-gallery" data-target="#modal-gallery"></tbody></table>
                </form>

                <!-- The template to display files available for upload -->
                <script id="template-upload" type="text/x-tmpl">
                {% for (var i=0, file; file=o.files[i]; i++) { %}
                    <tr class="template-upload fade">
                        <td class="preview"><span class="fade"></span></td>
                        <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
                        {% if (file.error) { %}
                            <td class="error" colspan="2"><span class="label label-important"><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="error" defaultValue="Error"/></span> {%=file.error%}</td>
                        {% } else if (o.files.valid && !i) { %}
                            <td>
                                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
                            </td>
                            <td class="start">{% if (!o.options.autoUpload) { %}
                                <button class="btn btn-primary">
                                    <i class="icon-upload icon-white"></i>
                                    <span><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="start" defaultValue="Start"/></span>
                                </button>
                            {% } %}</td>
                        {% } else { %}
                            <td colspan="2"></td>
                        {% } %}
                        <td class="cancel">{% if (!i) { %}
                            <button class="btn btn-warning">
                                <i class="icon-ban-circle icon-white"></i>
                                <span><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="cancel" defaultValue="Cancel"/></span>
                            </button>
                        {% } %}</td>
                    </tr>
                {% } %}
                </script>

                <script id="template-download" type="text/x-tmpl">
                {% for (var i=0, file; file=o.files[i]; i++) { %}
                    <tr class="template-download fade">
                        {% if (file.error) { %}
                            <td></td>
                            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
                            <td class="error" colspan="2"><span class="label label-important"><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="error" defaultValue="Error"/></span> {%=file.error%}</td>
                        {% } else { %}
                            <td class="preview">{% if (file.thumbnail_url) { %}
                                <a href="{%=file.url%}" title="{%=file.name%}" data-gallery="gallery" download="{%=file.name%}"><img src="{%=file.thumbnail_url%}"></a>
                            {% } %}</td>
                            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
                            <td colspan="2"></td>
                        {% } %}
                        <td class="delete">
                            <button class="btn btn-danger" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}"{% if (file.delete_with_credentials) { %} data-xhr-fields='{"withCredentials":true}'{% } %}>
                                <i class="icon-trash icon-white"></i>
                                <span><messages:message packageName="fr.untitled2.bundle.views.imageAdd" key="delete" defaultValue="Delete"/></span>
                            </button>
                            <input type="checkbox" name="delete" value="1">
                        </td>
                    </tr>
                {% } %}
                </script>

            </div>
        </div>
<jsp:include page="template/footer.jsp" />
        <script src="/resources/jqueryfileupload/vendor/jquery.ui.widget.js"></script>
        <!-- The Templates plugin is included to render the upload/download listings -->
        <script src="/resources/blueimp/tmpl.js"></script>
        <!-- The Load Image plugin is included for the preview images and image resizing functionality -->
        <script src="/resources/blueimp/load-image.min.js"></script>
        <!-- The Canvas to Blob plugin is included for image resizing functionality -->
        <script src="/resources/blueimp/canvas-to-blob.min.js"></script>
        <!-- Bootstrap JS and Bootstrap Image Gallery are not required, but included for the demo -->
        <script src="/resources/blueimp/bootstrap.min.js"></script>
        <script src="/resources/blueimp/bootstrap-image-gallery.min.js"></script>
        <!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
        <script src="/resources/jqueryfileupload/jquery.iframe-transport.js"></script>
        <!-- The basic File Upload plugin -->
        <script src="/resources/jqueryfileupload/jquery.fileupload.js"></script>
        <!-- The File Upload file processing plugin -->
        <script src="/resources/jqueryfileupload/jquery.fileupload-fp.js"></script>
        <!-- The File Upload user interface plugin -->
        <script src="/resources/jqueryfileupload/jquery.fileupload-ui.js"></script>
        <!-- The main application script -->
        <script src="/resources/jqueryfileupload/main.js"></script>
        <!-- The XDomainRequest Transport is included for cross-domain file deletion for IE8+ -->
        <!--[if gte IE 8]><script src="/resources/jqueryfileupload/cors/jquery.xdr-transport.js"></script><![endif]-->
    </body>
</html>
