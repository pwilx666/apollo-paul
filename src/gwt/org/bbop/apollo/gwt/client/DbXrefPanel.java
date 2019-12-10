package org.bbop.apollo.gwt.client;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.bbop.apollo.gwt.client.dto.AnnotationInfo;
import org.bbop.apollo.gwt.client.dto.DbXrefInfo;
import org.bbop.apollo.gwt.client.resources.TableResources;
import org.bbop.apollo.gwt.client.rest.DbXrefRestService;
import org.bbop.apollo.gwt.client.rest.ProxyRestService;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.extras.bootbox.client.Bootbox;
import org.gwtbootstrap3.extras.bootbox.client.callback.ConfirmCallback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by deepak.unni3 on 9/16/16.
 */
public class DbXrefPanel extends Composite {


    interface DbXrefPanelUiBinder extends UiBinder<Widget, DbXrefPanel> {
    }

    private static DbXrefPanelUiBinder ourUiBinder = GWT.create(DbXrefPanelUiBinder.class);

    DataGrid.Resources tablecss = GWT.create(TableResources.TableCss.class);
    @UiField(provided = true)
    DataGrid<DbXrefInfo> dataGrid = new DataGrid<>(200, tablecss);
    @UiField
    TextBox tagInputBox;
    @UiField
    TextBox valueInputBox;
    @UiField
    Button addDbXrefButton;
    @UiField
    Button deleteDbXrefButton;
    @UiField
    TextBox pmidInputBox;
    @UiField
    Button addPmidButton;

    private AnnotationInfo internalAnnotationInfo = null;
    private DbXrefInfo internalDbXrefInfo = null;
    private String oldTag, oldValue;
    private String tag, value;

    private static ListDataProvider<DbXrefInfo> dataProvider = new ListDataProvider<>();
    private static List<DbXrefInfo> dbXrefInfoList = dataProvider.getList();
    private SingleSelectionModel<DbXrefInfo> selectionModel = new SingleSelectionModel<>();
    EditTextCell tagCell = new EditTextCell();
    EditTextCell valueCell = new EditTextCell();

    public DbXrefPanel() {

        initWidget(ourUiBinder.createAndBindUi(this));

        dataGrid.setWidth("100%");
        initializeTable();
        dataProvider.addDataDisplay(dataGrid);
        dataGrid.setSelectionModel(selectionModel);
        selectionModel.clear();
        deleteDbXrefButton.setEnabled(false);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                if (selectionModel.getSelectedSet().isEmpty()) {
                    deleteDbXrefButton.setEnabled(false);
                } else {
                    selectDbXrefData(selectionModel.getSelectedObject());
                    deleteDbXrefButton.setEnabled(true);
                }
            }
        });

    }

    public void initializeTable() {
        Column<DbXrefInfo, String> tagColumn = new Column<DbXrefInfo, String>(tagCell) {
            @Override
            public String getValue(DbXrefInfo dbXrefInfo) {
                return dbXrefInfo.getTag();
            }
        };
        tagColumn.setFieldUpdater(new FieldUpdater<DbXrefInfo, String>() {
            @Override
            public void update(int i, DbXrefInfo object, String s) {
                if (s == null || s.trim().length() == 0) {
                    Bootbox.alert("Prefix can not be blank");
                    tagCell.clearViewData(object);
                    dataGrid.redrawRow(i);
                    redrawTable();
                } else if (!object.getTag().equals(s)) {
                    GWT.log("Tag Changed");
                    object.setTag(s);
                    selectDbXrefData(object);
                    updateDbXref();
                }
            }
        });
        tagColumn.setSortable(true);
        tagColumn.setDefaultSortAscending(true);

        Column<DbXrefInfo, String> valueColumn = new Column<DbXrefInfo, String>(valueCell) {
            @Override
            public String getValue(DbXrefInfo dbXrefInfo) {
                return dbXrefInfo.getValue();
            }
        };
        valueColumn.setFieldUpdater(new FieldUpdater<DbXrefInfo, String>() {
            @Override
            public void update(int i, DbXrefInfo object, String s) {
                if (s == null || s.trim().length() == 0) {
                    Bootbox.alert("Accession can not be blank");
                    valueCell.clearViewData(object);
                    dataGrid.redrawRow(i);
                    redrawTable();
                } else if (!object.getValue().equals(s)) {
                    GWT.log("Value Changed");
                    object.setValue(s);
                    selectDbXrefData(object);
                    updateDbXref();
                }
            }
        });
        valueColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        valueColumn.setSortable(true);

        dataGrid.addColumn(tagColumn, "Prefix");
        dataGrid.setColumnWidth(0, "100px");
        dataGrid.addColumn(valueColumn, "Accession");
        dataGrid.setColumnWidth(1, "100%");

        ColumnSortEvent.ListHandler<DbXrefInfo> sortHandler = new ColumnSortEvent.ListHandler<DbXrefInfo>(dbXrefInfoList);
        dataGrid.addColumnSortHandler(sortHandler);

        sortHandler.setComparator(tagColumn, new Comparator<DbXrefInfo>() {
            @Override
            public int compare(DbXrefInfo o1, DbXrefInfo o2) {
                return o1.getTag().compareTo(o2.getTag());
            }
        });

        sortHandler.setComparator(valueColumn, new Comparator<DbXrefInfo>() {
            @Override
            public int compare(DbXrefInfo o1, DbXrefInfo o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        // default is ascending
        dataGrid.getColumnSortList().push(tagColumn);
        ColumnSortEvent.fire(dataGrid, dataGrid.getColumnSortList());
    }

    public void updateData(AnnotationInfo annotationInfo) {
        GWT.log("updating annotation info: " + annotationInfo);
        if (annotationInfo == null) {
            return;
        }
        this.internalAnnotationInfo = annotationInfo;
        dbXrefInfoList.clear();
        dbXrefInfoList.addAll(annotationInfo.getDbXrefList());
        ColumnSortEvent.fire(dataGrid, dataGrid.getColumnSortList());
        GWT.log("List size: " + dbXrefInfoList.size());
        redrawTable();
        setVisible(true);
    }

    public void updateData() {
        updateData(null);
    }

    public void selectDbXrefData(DbXrefInfo v) {
        this.internalDbXrefInfo = v;
        // tag
        this.oldTag = this.tag;
        this.tag = this.internalDbXrefInfo.getTag();

        // value
        this.oldValue = this.value;
        this.value = this.internalDbXrefInfo.getValue();

        redrawTable();
        setVisible(true);
    }

    public void updateDbXref() {
        if (validateTags()) {
            final DbXrefInfo newDbXrefInfo = new DbXrefInfo(this.tag, this.value);
            RequestCallback requestCallBack = new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    JSONValue returnValue = JSONParser.parseStrict(response.getText());
                    redrawTable();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    tagCell.clearViewData(newDbXrefInfo);
                    dataGrid.redraw();
                    Bootbox.alert("Error updating variant info property: " + exception);
                    resetTags();
                    // TODO: reset data
                    redrawTable();
                }
            };
            DbXrefRestService.updateDbXref(requestCallBack, this.internalAnnotationInfo, new DbXrefInfo(this.oldTag, this.oldValue), newDbXrefInfo);
            ;
        } else {
            resetTags();
        }
    }

    private void resetTags() {
        GWT.log("reseting tag");
        this.tag = this.oldTag;
        this.value = this.oldValue;
        updateData(this.internalAnnotationInfo);
        redrawTable();
    }

    public void redrawTable() {
        this.dataGrid.redraw();
    }

    @UiHandler("tagInputBox")
    public void tagInputBoxType(KeyUpEvent event) {
        addDbXrefButton.setEnabled(validateTags());
    }

    @UiHandler("pmidInputBox")
    public void pmidInputBoxType(KeyUpEvent event) {
        addPmidButton.setEnabled(validatePmidTags());
    }


    @UiHandler("valueInputBox")
    public void valueInputBoxType(KeyUpEvent event) {
        addDbXrefButton.setEnabled(validateTags());
    }

    private boolean validatePmidTags() {
        collectPmidTags();
        return this.tag != null && !this.tag.isEmpty() && this.value != null && !this.value.isEmpty();
    }

    private void collectPmidTags() {
        this.tag = "PMID";
        this.value = pmidInputBox.getText();
    }

    private boolean validateTags() {
        collectTags();
        return this.tag != null && !this.tag.isEmpty() && this.value != null && !this.value.isEmpty();
    }

    private void collectTags() {
        this.tag = tagInputBox.getText();
        this.value = valueInputBox.getText();
    }

    @UiHandler("addPmidButton")
    public void addPmidButton(ClickEvent ce) {
        final AnnotationInfo internalAnnotationInfo = this.internalAnnotationInfo;
        final String pmidValue = this.value;
        if (validatePmidTags()) {
            final DbXrefInfo newDbXrefInfo = new DbXrefInfo(this.tag, this.value);
            this.tagInputBox.clear();
            this.valueInputBox.clear();
            this.pmidInputBox.clear();

            final RequestCallback requestCallBack = new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    JSONValue returnValue = JSONParser.parseStrict(response.getText());
                    List<DbXrefInfo> newList = new ArrayList<>(dbXrefInfoList);
                    newList.add(newDbXrefInfo);
                    internalAnnotationInfo.setDbXrefList(newList);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Bootbox.alert("Error updating variant info property: " + exception);
                    resetTags();
                    redrawTable();
                }
            };

            RequestCallback validationCallBack = new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    JSONValue returnValue = JSONParser.parseStrict(response.getText());
                    String title = null;
                    try {
                        title = returnValue.isObject().get("PubmedArticleSet").isObject().get("PubmedArticle").isObject().get("MedlineCitation").isObject().get("Article").isObject().get("ArticleTitle").isString().stringValue();
                    } catch (Exception e) {
                        GWT.log("Could not find error: "+e.getMessage() );
                        GWT.log("Return object "+returnValue.toString() );
                        Bootbox.alert("No article found for " + pmidValue);
                        resetTags();
                        redrawTable();
                        return;
                    }
                    Bootbox.confirm("Add article " + title, new ConfirmCallback() {
                        @Override
                        public void callback(boolean result) {
                            if (result) {
                                DbXrefRestService.addDbXref(requestCallBack, internalAnnotationInfo, newDbXrefInfo);
                            }
                            else{
                                resetTags();
                                redrawTable();
                            }
                        }
                    });
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Bootbox.alert("invalid PMID: " + pmidValue);

                }
            };
            ProxyRestService.findPubMedId(validationCallBack, pmidValue);

        }
    }


    @UiHandler("addDbXrefButton")
    public void addDbXrefButton(ClickEvent ce) {
        final AnnotationInfo internalAnnotationInfo = this.internalAnnotationInfo;
        if (validateTags()) {
            final DbXrefInfo newDbXrefInfo = new DbXrefInfo(this.tag, this.value);
            this.tagInputBox.clear();
            this.valueInputBox.clear();
            this.pmidInputBox.clear();

            RequestCallback requestCallBack = new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    JSONValue returnValue = JSONParser.parseStrict(response.getText());
                    List<DbXrefInfo> newList = new ArrayList<>(dbXrefInfoList);
                    newList.add(newDbXrefInfo);
                    internalAnnotationInfo.setDbXrefList(newList);
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Bootbox.alert("Error updating variant info property: " + exception);
                    resetTags();
                    // TODO: reset data
                    redrawTable();
                }
            };
            DbXrefRestService.addDbXref(requestCallBack, this.internalAnnotationInfo, newDbXrefInfo);
        }
    }

    @UiHandler("deleteDbXrefButton")
    public void deleteDbXref(ClickEvent ce) {

        if (this.internalDbXrefInfo != null) {
            RequestCallback requestCallBack = new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    JSONValue returnValue = JSONParser.parseStrict(response.getText());
                    deleteDbXrefButton.setEnabled(false);
                    redrawTable();
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Bootbox.alert("Error deleting variant info property: " + exception);
                    redrawTable();
                }
            };
            DbXrefRestService.deleteDbXref(requestCallBack, this.internalAnnotationInfo, this.internalDbXrefInfo);
            ;
        }
    }
}