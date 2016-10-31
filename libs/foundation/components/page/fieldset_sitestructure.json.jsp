<%@page session="false"
            import="com.day.cq.i18n.I18n"%>{
    collapsed: true,
    collapsible: true,
    title: CQ.I18n.get("Site Structure"),
    xtype: "dialogfieldset",
    items: [{
        fieldLabel: CQ.I18n.get("Login Page"),
        name: "./cq:loginPage",
        xtype: "pathfield"
    },{
        fieldLabel: CQ.I18n.get("Profile Page"),
        name: "./cq:profilePage",
        xtype: "pathfield"
    },{
        fieldLabel: CQ.I18n.get("Public Profile Page"),
        name: "./cq:socialProfilePage",
        xtype: "pathfield"
    },{
        fieldLabel: CQ.I18n.get("Signup Page"),
        name: "./cq:signupPage",
        xtype: "pathfield"
    },{
        fieldLabel: CQ.I18n.get("Shopping Cart Page"),
        name: "./cq:cartPage",
        xtype: "pathfield"
    },{
        fieldLabel: CQ.I18n.get("Checkout Page"),
        name: "./cq:checkoutPage",
        xtype: "pathfield"
    },{
        cls: "x-form-item-description",
        text: CQ.I18n.get("Note: blank paths will inherit values from parent page(s)."),
        xtype: "static"
    }]
}
