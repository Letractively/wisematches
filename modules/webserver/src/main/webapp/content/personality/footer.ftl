<#include "/core.ftl">
<#include "/content/templates/addthis.ftl"/>

<@wm.ui.panel.roundBottom class="footer">
<span class="copyrights" style="padding-right: 10px"><@message "copyrights.label"/></span>
<a href="/info/terms"><@message "info.policies.terms.label"/></a>
&nbsp;-&nbsp;
<a href="/info/policy"><@message "info.policies.policy.label"/></a>
&nbsp;-&nbsp;
<a href="/info/help"><@message "info.problems.label"/></a>
<div style="display: inline-block; vertical-align: bottom; padding-left: 10px">
    <@addthis counter=true/>
</div>
</@wm.ui.panel.roundBottom>