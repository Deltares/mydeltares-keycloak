<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        ${msg("termsAndPrivacyTitle")}
    <#elseif section = "form">
    <div id="kc-terms-text">
        ${kcSanitize(msg("termsText"))?no_esc}
    </div>
    <form class="form-actions" action="${url.loginAction}" method="POST">
        <div class="checkbox">
            <input id="acceptTerms" name="acceptTerms" type="checkbox" onchange="enableAccept()">${kcSanitize(msg("acceptTerms"))?no_esc}
            <span class="required">*</span>
        </div>
        <div class="checkbox">
            <input id="acceptPrivacy" name="acceptPrivacy" type="checkbox" onchange="enableAccept()">${kcSanitize(msg("acceptPrivacy"))?no_esc}
            <span class="required">*</span>
        </div>
        <br />
        <div class="checkbox">
            <input id="acceptSubscriptions" name="acceptSubscriptions" type="checkbox" >${kcSanitize(msg("acceptSubscriptions"))?no_esc}
        </div>
        <br />
        <div class="col-md-2 subtitle">
            <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
        </div>
        <br />
        <input disabled class="${properties.kcButtonClass!} ${properties.kcButtonSecondaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="${msg("doAccept")}"/>
        <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" name="cancel" id="kc-decline" type="submit" value="${msg("doDecline")}"/>
    </form>
    <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>
