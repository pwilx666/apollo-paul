<%@ page import="org.bbop.apollo.Feature" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="report">
    <title>${organism.commonName} Sequences</title>

    <script>
        function changeOrganism() {
            var name = $("#organism option:selected").val();
            window.location.href = "${createLink(action: 'report')}/" + name;
        }
    </script>
</head>

<body>

<g:render template="../layouts/reportHeader"/>

<div id="list-track" class="form-group report-header content scaffold-list" role="main">
    <div class="row form-group">
        <div class="col-lg-4 lead">${organism.commonName} Sequences</div>

        <g:select id="organism" class="input-lg" name="organism"
                  from="${org.bbop.apollo.Organism.listOrderByCommonName()}" optionValue="commonName" optionKey="id"
                  value="${organism.id}"
                  onchange=" changeOrganism(); "/>
    </div>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <table>
        <thead>
        <tr>
            <g:sortableColumn property="name" title="Name"/>
            <g:sortableColumn property="length" title="Length"/>
            <th>Annotators</th>
            <th>Top Level Features</th>
            <th>Genes</th>
            <th>Transcripts</th>
            <th>Exons</th>
            <th>TE</th>
            <th>RR</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${sequenceInstanceList}" status="i" var="sequenceInstance">
            <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">

                <td>
                    <g:link action="show"
                            id="${sequenceInstance.id}">${sequenceInstance.name}</g:link></td>
                <td style="text-align: left;">
                    <g:formatNumber number="${sequenceInstance.length}" type="number"/>
                </td>
                <td>
                    <g:each in="${sequenceInstance.annotators}" var="annotator">
                        <g:link action="report" controller="user" id="${annotator.id}">${annotator.username}</g:link>
                    </g:each>
                </td>
                <td>
                    ${sequenceInstance.totalFeatureCount}
                </td>
                <td>${sequenceInstance.geneCount}</td>
                <td>
                    <g:if test="${sequenceInstance.transcriptCount}">
                        <button>
                            Total
                            <span class="badge">${sequenceInstance.transcriptCount}</span>
                        </button>
                        <button>
                            Protein encoding
                            <span class="badge"><g:formatNumber number="${sequenceInstance.proteinCodingTranscriptPercent}" type="percent"/></span>
                        </button>
                        <button>
                            Exons / transcript
                            <span class="badge"><g:formatNumber number="${sequenceInstance.exonsPerTranscript}" type="number"/></span>
                        </button>

                        <g:each in="${sequenceInstance.transcriptTypeCount}" var="trans">
                            <button>
                                ${trans.key}
                                <span class="badge">
                                    ${trans.value}
                                </span>
                            </button>
                        </g:each>
                    </g:if>
                    <g:else>0</g:else>
                </td>
                <td>${sequenceInstance.exonCount} </td>
                <td>${sequenceInstance.transposableElementCount}</td>
                <td>${sequenceInstance.repeatRegionCount}</td>

            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="pagination">
        <g:paginate total="${sequenceInstanceCount ?: 0}"/>
    </div>
</div>

</body>
</html>
