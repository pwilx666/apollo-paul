package org.bbop.apollo.gwt.client.oracles;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import org.bbop.apollo.gwt.client.go.GoEvidenceCode;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ndunn on 4/24/15.
 */
public class BiolinkOntologyOracle extends MultiWordSuggestOracle {

    private final Integer ROWS = 40;
    private final String BIOLINK_AUTOCOMPLETE_URL = "http://api.geneontology.org/api/search/entity/autocomplete/";
    public final static String ECO_BASE = "http://www.evidenceontology.org/term/";
    public final static String GO_BASE = "http://amigo.geneontology.org/amigo/term/";
    public final static String RO_BASE = "http://www.ontobee.org/ontology/RO?iri=http://purl.obolibrary.org/obo/";

    private String prefix;
    private String baseUrl;
    private String category = null;
    private JSONArray preferredSuggestions = new JSONArray();
    private Boolean usePreferredSuggestions = true;
    private Boolean useAllEco = false;

    public BiolinkOntologyOracle() {
        this(BiolinkLookup.ECO, ECO_BASE);
    }

    public BiolinkOntologyOracle(BiolinkLookup biolinkLookup) {
        this(biolinkLookup, null);
    }

    public BiolinkOntologyOracle(BiolinkLookup biolinkLookup, String baseUrl) {
        super();
        this.prefix = biolinkLookup.name();
        GWT.log("initting Oracle with " + baseUrl + " " + this.prefix + " " + biolinkLookup);
        if (baseUrl != null) {
            this.baseUrl = baseUrl;
        } else {
            switch (biolinkLookup) {
                case ECO:
                    this.baseUrl = ECO_BASE;
                    break;
                case GO:
                    this.baseUrl = GO_BASE;
                    break;
                case RO:
                    this.baseUrl = RO_BASE;
                    break;
                default:
                    this.baseUrl = null;
            }
        }
    }

    public void addPreferredSuggestion(String name, String label, String id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", new JSONString(name));
        jsonObject.put("label", new JSONString(label));
        jsonObject.put("id", new JSONString(id));
        this.preferredSuggestions.set(this.preferredSuggestions.size(), jsonObject);
//        GWT.log("added: " + preferredSuggestions.size() + " " + jsonObject.toString());
    }

    private void requestRemoteData(final Request suggestRequest, final Callback suggestCallback) {
        String url = BIOLINK_AUTOCOMPLETE_URL + suggestRequest.getQuery() + "?rows=" + ROWS;
        if (prefix != null) {
            url += "&prefix=" + prefix;
        }
        if (category != null) {
            url += "&category=" + category;
        }
        RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, url);
        try {

            rb.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(com.google.gwt.http.client.Request request, com.google.gwt.http.client.Response response) {
                    JSONArray jsonArray = JSONParser.parseStrict(response.getText()).isObject().get("docs").isArray();
                    List<Suggestion> suggestionList = new ArrayList<>();
                    Set<String> ids = new HashSet<>();

                    for (int i = 0; i < jsonArray.size(); i++) {
                        final JSONObject jsonObject = jsonArray.get(i).isObject();
                        final String id = jsonObject.get("id").isString().stringValue();

                        if (!ids.contains(id)) {
                            Suggestion suggestion = new Suggestion() {
                                @Override
                                public String getDisplayString() {
                                    String displayString = jsonObject.get("label").isArray().get(0).isString().stringValue();
                                    displayString = displayString.replaceAll(suggestRequest.getQuery(), "<b><em>" + suggestRequest.getQuery() + "</em></b>");
                                    displayString += " (" + id + ") ";
                                    return displayString;
                                }

                                @Override
                                public String getReplacementString() {
                                    return id;
                                }
                            };
                            suggestionList.add(suggestion);
                        }
                    }

                    Response r = new Response();
                    r.setSuggestions(suggestionList);
                    suggestCallback.onSuggestionsReady(suggestRequest, r);
                }

                @Override
                public void onError(com.google.gwt.http.client.Request request, Throwable exception) {
                    Bootbox.alert("Error: " + exception);
                }
            });
        } catch (RequestException e) {
            e.printStackTrace();
            Bootbox.alert("Request exception via " + e);
        }
    }

    private void requestDefaultGo(final Request suggestRequest, final Callback suggestCallback) {
        List<Suggestion> suggestionList = new ArrayList<>();
        String query = suggestRequest.getQuery().toLowerCase().trim();
        for (final GoEvidenceCode goEvidenceCode : GoEvidenceCode.values()) {
            if (goEvidenceCode.name().toLowerCase().contains(query)
                    || goEvidenceCode.getCurie().toLowerCase().contains(query)
                    || goEvidenceCode.getDescription().toLowerCase().contains(query)
                    || query.length()==0
            ) {
                Suggestion suggestion = new Suggestion() {
                    @Override
                    public String getDisplayString() {
                        String displayString = goEvidenceCode.name() + " (" + goEvidenceCode.getCurie() +"): " + goEvidenceCode.getDescription();
                        displayString = displayString.replaceAll(suggestRequest.getQuery(), "<em>" + suggestRequest.getQuery() + "</em>");
                        displayString = "<div style='font-weight:boldest;font-size:larger;'>" + displayString + "</div>";
                        return displayString;
                    }

                    @Override
                    public String getReplacementString() {
                        return goEvidenceCode.getCurie();
                    }
                };
                suggestionList.add(suggestion);
            }
        }
        GWT.log("list size:" + query + " vs " + suggestionList.size());
        Response r = new Response();
        r.setSuggestions(suggestionList);
        suggestCallback.onSuggestionsReady(suggestRequest, r);
    }


    @Override
    public void requestSuggestions(final Request suggestRequest, final Callback suggestCallback) {

        if (!useAllEco && baseUrl.equals(ECO_BASE)) {
            requestDefaultGo(suggestRequest, suggestCallback);
        } else {
            requestRemoteData(suggestRequest, suggestCallback);
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getUsePreferredSuggestions() {
        return usePreferredSuggestions;
    }

    public void setUsePreferredSuggestions(Boolean usePreferredSuggestions) {
        this.usePreferredSuggestions = usePreferredSuggestions;
    }

    public void setUseAllEco(Boolean useAllEco) {
        this.useAllEco = useAllEco;
    }

    public Boolean getUseAllEco() {
        return useAllEco;
    }
}
