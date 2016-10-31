<%--
/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2011 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
--%><%@page session="false" contentType="text/html"
            pageEncoding="utf-8"
            import="com.day.cq.widget.HtmlLibraryManager" %><%!

%><%@include file="/libs/foundation/global.jsp"%><%
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN">
<html>
<head>
    <title>Portlet Preferences</title>
    <meta http-equiv="Content-Type" content="text/html; utf-8" />
    <%
    HtmlLibraryManager htmlMgr = sling.getService(HtmlLibraryManager.class);
    if (htmlMgr != null) {
        htmlMgr.writeIncludes(slingRequest, out, "cq.widgets");
    }
    %>
    <script type="text/javascript">
CQ.portlets = {};
CQ.portlets.createPage = function(){
    var parentPath = CQ.Ext.getCmp("cqportletprefspathfield").getValue();
    var templatesStore = new CQ.Ext.data.Store({
        "proxy": new CQ.Ext.data.HttpProxy({ "url":"/bin/wcm/templates", "method":"GET" }),
        "reader": new CQ.Ext.data.JsonReader(
            { "totalProperty":"results", "root":"templates", "id":"path" },
            [ "path", "title", "description", "thumbnailPath", "iconPath", "ranking" ]
        ),
        "baseParams": { "_charset_":"utf-8", "path":parentPath },
        "listeners": {
            "load": function() {
                this.sort("ranking");
            }
        }

    });

    var dataView = new CQ.Ext.DataView({
        "multiSelect": false,
        "singleSelect": true,
        "emptyText": CQ.I18n.getMessage("No template available"),
        "store": templatesStore,
        "overClass": "x-view-over",
        "itemSelector" :"div.template-item",
        "tpl":new CQ.Ext.XTemplate(
            '<tpl for=".">',
            '<div class="template-item">',
            '<tpl if="thumbnailPath">',
                '<img class="template-thumbnail" src="{thumbnailPath}">',
                '<div class="template-title">{title}</div>',
                '<div class="template-description">{description}</div>',
            '</tpl>',
            '<tpl if="thumbnailPath == \'\'">',
                '<div class="template-title template-no-thumbnail">{title}</div>',
                '<div class="template-description template-no-thumbnail">{description}</div>',
            '</tpl>',
            '<div style="clear:both"></div>',
            '</div>',
            '</tpl>',
            '<div style="height:5px;overflow:hidden"></div>'),
        "prepareData": function(data) {
            // 900000000: move to the end of the list
            data.ranking = data.ranking != null ? data.ranking : 900000000;
            data.thumbnailPath = data.thumbnailPath
                    ? CQ.shared.HTTP.getXhrHookedURL(CQ.HTTP.externalize(data.thumbnailPath))
                    : CQ.Ext.BLANK_IMAGE_URL;
            data.title = CQ.I18n.getVarMessage(data.title);
            data.description = data.description ? CQ.I18n.getVarMessage(data.description) : "";
            return data;
        }
    });

    var hiddenTemplate =  new CQ.Ext.form.Hidden({"name": "template"});

    //workaround to select a default value. select() must be called at the end of refresh method
    dataView.refresh = function(){
        this.clearSelections(false, true);
        this.el.update("");
        var html = [];
        var records = this.store.getRange();
        if(records.length < 1){
            if(!this.deferEmptyText || this.hasSkippedEmptyText){
                this.el.update(this.emptyText);
            }
            this.hasSkippedEmptyText = true;
            this.all.clear();
            return;
        }
        this.tpl.overwrite(this.el, this.collectData(records, 0));
        this.all.fill(CQ.Ext.query(this.itemSelector, this.el.dom));
        this.updateIndexes(0);

        //CQ: START
        //select first item by default
        this.select(0);
        //CQ: END
    };

   var createDialog = {
        "jcr:primaryType": "cq:Dialog",
        "id": CQ.Util.createId("cq-createdialog"),
        "title":CQ.I18n.getMessage("Create Page"),
        "formUrl":CQ.HTTP.externalize("/bin/wcmcommand"),
        "params": {
            "cmd":"createPage",
            "_charset_":"utf-8"
        },
        "height": 520,
        "items": {
            "jcr:primaryType": "cq:Panel",
            "items": {
                "jcr:primaryType": "cq:WidgetCollection",
                "title": {
                    "fieldLabel":CQ.I18n.getMessage("Title"),
                    "allowBlank":false,
                    "name":"title"
                },
                "label": {
                    "fieldLabel":CQ.I18n.getMessage("Name"),
                    "vtype":"itemname",
                    "name":"label"
                },
                "template": {
                    "xtype": "panel",
                    "border": false,
                    "cls": "cq-template-view",
                    "autoScroll":true,
                    "width": "100%",
                    "autoHeight":false,
                    "height": 350,
                    "items": [
                        hiddenTemplate,
                        dataView
                    ]
                    ,"listeners": {
                        "render" : {
                            fn: function() {
                                templatesStore.load();
                            }
                        }
                    }
                }
            }
        },
        "okText":CQ.I18n.getMessage("Create")
    };

    var dialog = CQ.WCM.getDialog(createDialog);

    dialog.on("beforesubmit", function() {
        if(dataView.getSelectedRecords()[0] && dataView.getSelectedRecords()[0].data)
            hiddenTemplate.setRawValue(dataView.getSelectedRecords()[0].data.path);
    });

    dialog.addHidden({ "parentPath":parentPath });
    dialog.success = function(form, action) {
        var path = action.result["Location"];
        if ((path.length > 7) && (path.substring(0, 7) == "http://")) {
            var hostEndPos = path.indexOf("/", 7);
            path = path.substring(hostEndPos);
        }
        var search = CQ.portlets.prefix;
        var pos = path.indexOf(search);
        if ((path.length > CQ.portlets.prefixlength)
            && (path.substring(0, CQ.portlets.prefixlength) == CQ.portlets.prefix)) {
            path = path.substring(pos + CQ.portlets.prefixlength);
        }
        var parPos = path.indexOf("?");
        if ( parPos > 0 ) {
            path = path.substring(0, parPos);
        }
        CQ.Ext.getCmp("cqportletprefspathfield").setValue(path);
    };
    dialog.failure = function(){
        CQ.Ext.Msg.alert(
            CQ.I18n.getMessage("Error"),
            CQ.I18n.getMessage("Could not create page.")
        );
    };

    dialog.show();
};
CQ.Ext.onReady(function(){
   PORTLET_PREFS
   CQ.portlets.prefixlength = data.prefixLength;
   CQ.portlets.prefix = data.prefix;
   var redirectCancel = data.redirectCancel;
   var createDialog = {
        "jcr:primaryType": "cq:Dialog",
        "title":CQ.I18n.getMessage("Portlet Preferences"),
        "formUrl":data.url,
        "height": 520,
        "modal":true,
        "items": {
            "jcr:primaryType": "cq:Panel",
            "items": {
                "jcr:primaryType": "cq:WidgetCollection",
                "path": {
                    "fieldLabel":CQ.I18n.getMessage("Start Path"),
                    "allowBlank":false,
                    "name":"startPath",
                    "xtype":"pathfield",
                    "id": "cqportletprefspathfield",
                    "rootPath":"/",
                    "value": data.startPath
                },
                "create": {
                    "xtype":"cq.compositefield",
                    "border":false,
                    "items":[
                    {
                        "xtype":"panel",
                        "border":false,
                        "bodyStyle":"padding:4px",
                        "items":[{
                            "xtype":"button",
                            "text":CQ.I18n.getMessage("Create Page"),
                            "handler":function() {
                                CQ.portlets.createPage();
                            }
                        }]
                    }]
                },
                "selector": {
                    "fieldLabel":CQ.I18n.getMessage("HTML Selector"),
                    "name":"htmlSelector",
                    "xtype":"textfield",
                    "value": data.htmlSelector
                },
                "urlParamNames": {
                    "fieldLabel":CQ.I18n.getMessage("URL Parameters"),
                    "name" : "urlParameterNames",
                    "xtype":"textfield",
                    "value": data.urlParameterNames
                },
                "privToolbar": {
                    "fieldLabel":CQ.I18n.getMessage("Priv Toolbar"),
                    "name" : "priv:toolbar",
                    "xtype":"textfield",
                    "value": data["priv:toolbar"]
                },
                "privPrefs": {
                    "fieldLabel":CQ.I18n.getMessage("Priv Prefs"),
                    "name" : "priv:prefs",
                    "xtype":"textfield",
                    "value": data["priv:prefs"]
                },
                "privAuthorEdit": {
                    "fieldLabel":CQ.I18n.getMessage("Priv Author Edit"),
                    "name" : "priv:cq-author:edit",
                    "xtype":"textfield",
                    "value": data["priv:cq-author:edit"]
                },
                "privAuthorPreview": {
                    "fieldLabel":CQ.I18n.getMessage("Priv Author Preview"),
                    "name" : "priv:cq-author:preview",
                    "xtype":"textfield",
                    "value": data["priv:cq-author:preview"]
                },
                "privAuthorSiteadmin": {
                    "fieldLabel":CQ.I18n.getMessage("Priv Author SiteAdmin"),
                    "name" : "priv:cq-author:siteadmin",
                    "xtype":"textfield",
                    "value": data["priv:cq-author:siteadmin"]
                },
                "cssHeader":{
                    "fieldLabel":CQ.I18n.getMessage("Include CSS"),
                    "name":"addCssToPortalHeader",
                    "type":"checkbox",
                    "xtype":"selection",
                    "value": data.addCssToPortalHeader
                },
                "includeToolbar":{
                    "fieldLabel":CQ.I18n.getMessage("Include Toolbar"),
                    "name":"includeToolbar",
                    "type":"checkbox",
                    "xtype":"selection",
                    "value": data.includeToolbar
                },
                "cfWidth": {
                    "fieldLabel":CQ.I18n.getMessage("iframe width (CF)"),
                    "name" : "cfWidth",
                    "xtype":"textfield",
                    "value": data["cfWidth"]
                },
                "cfHeight": {
                    "fieldLabel":CQ.I18n.getMessage("iframe height (CF)"),
                    "name" : "cfHeight",
                    "xtype":"textfield",
                    "value": data["cfHeight"]
                }
            }
        },
        "okText":CQ.I18n.getMessage("Change")
    };
    var dialog = CQ.WCM.getDialog(createDialog);
    dialog.on("hide", function() {
        window.location = redirectCancel;
    });
    dialog.addHidden({ "cqp:prefs":"true", "cqp:action":"prefs", "urlParameterNames@TypeHint":"String[]"});
    dialog.ok = function() {
        var form = this.form.el.dom;
        form.action = this.form.url;
        form.submit();
    };
    dialog.show();
});
</script>
</head>
<body>
</body>
</html>