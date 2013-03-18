<#-- @ftlvariable name="context.award" type="wisematches.server.services.award.Award" -->
<#-- @ftlvariable name="context.player" type="wisematches.core.Personality" -->
<#-- @ftlvariable name="context.descriptor" type="wisematches.server.services.award.AwardDescriptor" -->
<#import "../../utils.ftl" as notify>

<p xmlns="http://www.w3.org/1999/html">
    Congratulations!
</p>

<p>
    You have received
    new <#if !context.descriptor.type.ribbon>
    <strong>${messageSource.getMessage("awards." + context.award.weight.name()?lower_case + ".label", locale)}</strong></#if>
    award <strong>"${messageSource.getMessage("awards." + context.award.code+ ".label", locale)}"</strong>.
</p>

<p>
    You can check all received awards at <@notify.link "playground/profile/awards?p=${context.player.id?string}">your
    profile</@notify.link>.
</p>