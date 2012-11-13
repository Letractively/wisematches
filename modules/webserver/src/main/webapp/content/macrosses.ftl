<#include "/core.ftl">

<#macro jstable>
<link rel="stylesheet" type="text/css" href="/jquery/css/table_jui.css"/>
<script type="text/javascript" src="/jquery/js/jquery.dataTables.min.js"></script>
<link rel="stylesheet" type="text/css" href="/jquery/css/ColReorder.css"/>
<script type="text/javascript" src="/jquery/js/ColReorder.min.js"></script>
<link rel="stylesheet" type="text/css" href="/jquery/css/ColVis.css"/>
<script type="text/javascript" src="/jquery/js/ColVis.min.js"></script>

<script type="text/javascript">
    wm.ui.dataTable = function (selector, opts) {
        var dataTableLanguage = {
            "sUrl": "",
            "bJQueryUI": true,
            "sInfoPostFix": "",
            "sPaginationType": "full_numbers",
            "sDom": '<"data-table-top"<"ui-widget-content">><"data-table-content"t><"data-table-bottom"<"ui-widget-content"rlip>>',
            "oLanguage": {
                "sProcessing": "<@message code="datatable.processing.label"/>",
                "sLengthMenu": "<@message code="datatable.menu.label"/>",
                "sZeroRecords": "<@message code="datatable.zero.records.label"/>",
                "sInfo": "_START_ - _END_ <@message code="datatable.of.label"/> _TOTAL_",
                "sInfoEmpty": "<@message code="datatable.info.empty.label"/>",
                "sLoadingRecords": "<@message code="datatable.loading.records.label"/>",
                "oPaginate": {
                    "sFirst": "&nbsp;",
                    "sPrevious": "&nbsp",
                    "sNext": "&nbsp",
                    "sLast": "&nbsp"
                }
            }
        };
        return $(selector).dataTable($.extend(true, dataTableLanguage, opts));
    };
</script>
</#macro>

<#macro dtHeader class="" align="left">
<div class="data-table-header ui-widget-header ui-corner-top ${class}" style="text-align: ${align}"><#nested/></div>
</#macro>

<#macro dtToolbar class="" align="right">
<div class="data-table-toolbar ui-widget-content ${class}">
    <div class="ui-state-hover" style="text-align: ${align}"><#nested/></div>
</div>
</#macro>

<#macro dtContent class="" wrap=false>
    <#if wrap>
    <div class="data-table-content ui-widget-content ${class}"></#if><#nested/><#if wrap></div></#if>
</#macro>

<#macro dtStatusbar class="" align="right">
<div class="data-table-bottom ${class}">
    <div class="ui-widget-content" style="text-align: ${align}"><#nested/></div>
</div>
</#macro>

<#macro dtFooter class="">
<div class="data-table-footer ui-corner-bottom ${class}" style="padding: 5px"><#nested/></div>
</#macro>

<#macro playground id>
<table width="100%">
    <tr>
        <td width="165px" valign="top" align="left" rowspan="2">
            <#if principal.membership.adsVisible>
            <#assign advertisementBlock=advertisementManager.getAdvertisementBlock(id, locale)!""/>
            <#include "/content/templates/advertisement.ftl">
            </#if>
        </td>
        <td valign="top">
            <div <#if id?has_content>id="${id}"</#if>>
                <#nested/>
            </div>
        </td>
    </tr>
    <tr>
        <td><#include "playground/footer.ftl"/></td>
    </tr>
</table>
</#macro>

<#macro editor id code value="" view="" classes="">
    <#assign qwe=view/>
    <#if (!view?has_content) && (value?has_content)><#assign qwe=value/></#if>
<div id="${id}" class="ui-editor-item ${classes}">
    <div class="ui-editor-label ${classes}"><@message code="${code}.label"/></div>
    <div label="<@message code="${code}.description"/>"
         class=" ui-editor-view<#if !qwe?has_content> sample</#if> ${classes}
    "><#if qwe?has_content>${qwe}<#else><@message code="${code}.description"/></#if></div>
    <input name="${id}" type="hidden" value="${value}">
</div>
</#macro>

<#macro player player showType=true showState=true hideLink=false>
    <#assign computerPlayer=(player.membership == "GUEST") || (player.membership == "ROBOT")/>
<span class="player <#if computerPlayer>computer<#else>member</#if>">
    <#if showState && playerStateManager.isPlayerOnline(player)>
        <div class="online"></div></#if>
    <span>
    <#if !computerPlayer && !hideLink><a href="/playground/profile/view?p=${player.id}"></#if><span
            class="nickname">${gameMessageSource.getPlayerNick(player, locale)}</span>
        <#if showType && player.getMembership() != "BASIC">
            <span class="mod ${player.membership!""?lower_case}"></span></#if><#if !computerPlayer && !hideLink>
    </a></#if>
    </span>
</span>
</#macro>

<#macro info>
<div class="help-tooltip ui-icon ui-icon-info" title="<#nested>"></div>
</#macro>

<#macro tournament number short=false>
${number}${gameMessageSource.getNumeralEnding(number, locale)}
    <#if short><@message code="tournament.short.label"/><#else><@message code="tournament.long.label"/></#if>
</#macro>

<#macro restriction style="" showIcon=true showLabel=true>
<div class="restriction ui-state-error ui-corner-all shadow" <#if style?has_content>style="${style}"</#if>>
    <#if showIcon>
        <div class="restriction-icon wm-icon-forbidden"></div></#if>
    <#if showLabel>
        <div class="restriction-name"><@message code="restriction.label"/></div></#if>
    <div class="restriction-message"><#nested></div>
</div>
</#macro>

<#macro field path id="" class="">
    <@spring.bind path/>
<div <#if id?has_content>id="${id}"</#if>
     class="<#if spring.status.error>field-error<#else>field-ok</#if><#if class?has_content> ${class}</#if>">
    <#assign status=spring.status>
    <#assign statusValue=spring.stringStatusValue>

    <#nested >

    <#list status.errorMessages as msg>
        <div class="ui-state-error-text error-msg">${msg}</div>
    </#list>
</div>
</#macro>

<#macro fieldInput path attributes="" fieldType="text" size=30 value="">
    <@field path=path>
    <input type="${fieldType}" id="${spring.status.expression}" name="${spring.status.expression}" size="${size}"
        <#if fieldType=='checkbox'><#if spring.stringStatusValue?has_content && spring.stringStatusValue=='true'>checked="checked"</#if>
           value="true"<#else>
           value="<#if fieldType!="password"><#if spring.stringStatusValue?has_content>${spring.stringStatusValue}<#else><@message code=value/></#if></#if>"</#if> ${attributes}/>
        <#nested>
    </@field>
</#macro>

<#macro captcha path>
    <#if captchaService??><@field path>${captchaService.createCaptchaScript(gameMessageSource, locale)}</@field></#if>
</#macro>

<#macro widget title id="" class="" style="" help="" hidden=false>
<div class="ui-widget<#if class?has_content> ${class}</#if> <#if hidden>ui-helper-hidden</#if>"
     <#if style?has_content>style="${style}"</#if>>
    <div class="ui-widget-header ui-corner-all shadow">
        <#if help?has_content>
            <div class="quickInfo ajax">
                <a class="ui-icon ui-icon-info" href="#" onclick="return false" rel="/info/tip.ajax?s=${help}"></a>
            </div>
        </#if>
        <@message code=title/>
    </div>
    <div <#if id?has_content>id="${id}"</#if> class="ui-widget-content ui-corner-all shadow">
        <#nested/>
    </div>
</div>
</#macro>

<#macro topRoundPanel id="" class="" style="">
    <@abstractPanel id="${id}" class="ui-corner-top ${class}" style="${style}">
        <#nested>
    </@abstractPanel>
</#macro >

<#macro bottomRoundPanel id="" class="" style="">
    <@abstractPanel id="${id}" class="ui-corner-bottom ${class}" style="${style}">
        <#nested>
    </@abstractPanel>
</#macro >

<#macro roundPanel id="" class="" style="">
    <@abstractPanel id="${id}" class="ui-corner-all ${class}" style="${style}">
        <#nested>
    </@abstractPanel>
</#macro >

<#macro abstractPanel class style id="">
<div <#if id?has_content>id="${id}"</#if> class="ui-widget-content ${class!}"
     <#if style?has_content>style="${style}"</#if>>
    <#nested>
</div>
</#macro>