<#-- @ftlvariable name="notificationSettings" type="wisematches.server.web.services.notify.NotificationSettings" -->
<#-- @ftlvariable name="notificationDescriptors" type="java.util.Collection<wisematches.server.web.services.notify.NotificationDescriptor>" -->

<#include "/core.ftl">

<#assign lastGroup=""/>
<table class="common-settings ui-widget-content ui-state-default shadow ui-corner-all" style="background-image: none;"
       width="100%">
<#list notificationDescriptors as desc>
    <#if (desc_index !=0 && lastGroup!=desc.section)>
        <tr>
            <td colspan="2" class="ui-state-default shadow"></td>
        </tr>
    </#if>
    <#if lastGroup!=desc.section>
        <tr>
            <td colspan="2">
                <h2 style="margin-bottom: 0;">
                    <@message code="account.modify.notice.section.${desc.section?lower_case}"/>
                </h2>
            </td>
        </tr>
    </#if>
    <#assign lastGroup=desc.section/>
    <tr>
        <td style="padding-top: 4px; width: 10px;">
            <input id="field${desc.code}" name="${desc.code}" type="checkbox"
                   <#if notificationSettings.isEnabled(desc.code)>checked="checked"</#if> value="true">
        </td>
        <td>
            <div>
                <label for="field${desc.code}">
                    <@message code="account.modify.notice.${desc.code?lower_case}.label"/>
                </label>
            </div>
            <div class="sample">
                <@message code="account.modify.notice.${desc.code?lower_case}.description"/>
            </div>
        </td>
    </tr>
</#list>
    <tr>
        <td colspan="2" class="ui-state-default shadow"></td>
    </tr>
</table>