(function($) {
    var plugin = CQ.Ext.extend(CQ.Ext.emptyFn, {
        init: function(widget) {
            var locale = "en";

            // create some JS logic to get the locale here
            // current path can be obtained via
            // widget.findParentByType('dialog').responseScope.path
            widget.treeRoot.name = "content/Amos";
        }
    });

    CQ.Ext.ComponentMgr.registerPlugin('setRootNodePlugin', plugin);
}($CQ));