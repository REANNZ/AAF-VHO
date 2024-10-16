package virtualhome

/*class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}*/


class UrlMappings {

  static mappings = {

    // Mappings from applicationbase

    // Session Management
    "/session/federated/$action?/$id?"{
      controller = "federatedSessions"
    }

    "/session/$action?/$id?"{
      controller = "auth"
    }

    "/logout"{
      controller = "auth"
      action = "logout"
    }

    // Workflow
    "/workflow/approval/$action?/$id?" {
      controller="workflowApproval"
    }

    // Role invitation
    "/inviteadministrator/finalization/$inviteCode"{
      controller = "role" 
      action = "finalization"
    }

    "/inviteadministrator/error"{
      controller = "role" 
      action = "finalizationerror"
    }

    // Administration
    "/administration/dashboard"{
      controller="adminDashboard"
      action="index"
    }

    "/administration/environment"{
      controller="adminDashboard"
      action="environment"
    }

    "/administration/subjects/$action?/$id?"{
      controller = "subject" 
    }

    "/administration/apisubjects/$action?/$id?"{
      controller = "apiSubject" 
    }

    "/administration/roles/$action?/$id?"{
      controller = "role" 
    }

    "/administration/workflow/processes/$action?/$id?" {
      controller="workflowProcess"
    }

    "/administration/workflow/scripts/$action?/$id?" {
      controller="workflowScript"
    }

    "/administration/emailtemplates/$action?/$id?"{
      controller = "emailTemplate"
    }

    // Console plugin
    "/console"{
      controller = "console"
      action = "index"
    }

    "/console/$action"{
      controller = "console"
    }

    // Errors
    /*"403"(controller:'error', action:'notPermitted')
    "404"(controller:'error', action:'notFound')
    "405"(controller:'error', action:'notAllowed')
    "500"(controller:'error', action:'internalServerError')*/

    // Greenmail (Development mode only)
    "/greenmail/$action?/$id?"{
      controller = "greenmail"
    }

    // Fake SMS Delivery (Development mode only)
    "/sms/json"(controller:"fakeSMSDelivery", action:"json")

    // Mappings from virtualhome

    "/"(controller:"dashboard", action:"welcome")

    "/login" {
      controller="login"
      action="index"
    }
    "/login/$action?" {
      controller="login"
    }

    "/myaccount/setup/login-available" {
      controller="finalization"
      action="loginAvailable"
    }
    "/myaccount/setup/complete" {
      controller="finalization"
      action="complete"
    }
    "/myaccount/setup/used" {
      controller="finalization"
      action="used"
    }
    "/myaccount/setup/error" {
      controller="finalization"
      action="error"
    }
    "/myaccount/setup/$inviteCode" {
      controller="finalization"
    }
    "/myaccount" {
      controller="account"
      action="index"
    }
    "/myaccount/login" {
      controller="account"
      action="login"
    }
    "/myaccount/twosteplogin" {
      controller="account"
      action="twosteplogin"
    }
    "/myaccount/logout" {
      controller="account"
      action="logout"
    }
    "/myaccount/details" {
      controller="account"
      action="show"
    }
    "/myaccount/changedetails" {
      controller="account"
      action="changedetails"
    }
    "/myaccount/completedetailschange" {
      controller="account"
      action="completedetailschange"
    }
    "/myaccount/enabletwostep" {
      controller="account"
      action="enabletwostep"
    }
    "/myaccount/finishenablingtwostep" {
      controller="account"
      action="finishenablingtwostep"
    }

    "/lostpassword/$action" {
      controller="lostPassword"
    }
    "/lostusername/$action" {
      controller="lostUsername"
    }
    "/migration/$action" {
      controller="migrate"
    }

    "/dashboard"(controller:"dashboard", action:"dashboard")

    "/accounts/list" {
      controller="managedSubject"
      action="list"
    }
    "/groups/list" {
      controller="group"
      action="list"
    }

    "/organisations/groups/accounts/$action/$id?" {
      controller="managedSubject"
    }
    "/organisations/groups/accountstatus/$id" {
      controller="group"
      action="nonfinalized"
    }
    "/organisations/groups/$action/$id?" {
      controller="group"
    }
    "/organisations/$action/$id?" {
      controller="organization"
    }

    "/backend/manageadministrators/$action/$id?" {
      controller="manageAdministrators"
    }

    "/api/v1/login/confirmsession/$sessionID" (controller: "loginApi", action: "confirmsession")
    "/api/v1/login/basicauth" (controller: "loginApi", action:"basicauth")
    "/api/v1/organizations/$action/$id?" (controller: "organizationApi")
    "/api/v1/scopes/$action?" (controller: "scopesApi")
  }

}
