/*
 * jQuery File Upload Plugin JS Example 7.0
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/*jslint nomen: true, unparam: true, regexp: true */
/*global $, window, document */

$(function() {
    'use strict';

    // Initialize the jQuery File Upload widget:
    $('#fileupload').fileupload({
        // Uncomment the following to send cross-domain cookies:
        //xhrFields: {withCredentials: true},
        url: 'add'
    });

    $('#fileupload').fileupload('option', {
        url: 'add',
        maxFileSize: 100000000,
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png|nef)$/i,
        process: [
            {
                action: 'load',
                fileTypes: /^image\/(gif|jpeg|png|nef)$/,
                maxFileSize: 200000000 // 20MB
            },
            {
                action: 'save'
            }
        ]
    });

});

