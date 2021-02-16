package org.bbop.apollo.gwt.client.dto;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import org.bbop.apollo.gwt.client.VariantDetailPanel;
import org.bbop.apollo.gwt.shared.FeatureStringEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nathandunn on 7/14/15.
 */
public class AnnotationInfoConverter {


    public static List<AnnotationInfo> convertFromJsonArray(JSONArray array ) {
        List<AnnotationInfo> annotationInfoList = new ArrayList<>();

        for(int i = 0 ; i < array.size() ;i++){
            annotationInfoList.add(convertFromJsonObject(array.get(i).isObject()));
        }

        return annotationInfoList ;
    }

    public static AnnotationInfo convertFromJsonObject(JSONObject object) {
        return convertFromJsonObject(object, true);
    }

    public static AnnotationInfo convertFromJsonObject(JSONObject object, boolean processChildren) {
        AnnotationInfo annotationInfo = new AnnotationInfo();
        annotationInfo.setName(object.get(FeatureStringEnum.NAME.getValue()).isString().stringValue());
        annotationInfo.setType(object.get(FeatureStringEnum.TYPE.getValue()).isObject().get(FeatureStringEnum.NAME.getValue()).isString().stringValue());
        if (object.get(FeatureStringEnum.SYMBOL.getValue()) != null) {
            annotationInfo.setSymbol(object.get(FeatureStringEnum.SYMBOL.getValue()).isString().stringValue());
        }
        if (object.get(FeatureStringEnum.DESCRIPTION.getValue()) != null) {
            annotationInfo.setDescription(object.get(FeatureStringEnum.DESCRIPTION.getValue()).isString().stringValue());
        }
        if (object.get(FeatureStringEnum.SYNONYMS.getValue()) != null) {
            annotationInfo.setSynonyms(object.get(FeatureStringEnum.SYNONYMS.getValue()).isString().stringValue());
        }
        if (object.get(FeatureStringEnum.STATUS.getValue()) != null) {
            annotationInfo.setStatus(object.get(FeatureStringEnum.STATUS.getValue()).isString().stringValue());
        }
        if (VariantDetailPanel.variantTypes.contains(annotationInfo.getType())) {
            // If annotation is a variant annotation
            if (object.get(FeatureStringEnum.REFERENCE_ALLELE.getValue()) != null) {
                annotationInfo.setReferenceAllele(object.get(FeatureStringEnum.REFERENCE_ALLELE.getValue()).isObject().get(FeatureStringEnum.BASES.getValue()).isString().stringValue());
            }
            if (object.get(FeatureStringEnum.ALTERNATE_ALLELES.getValue()) != null) {
                annotationInfo.setAlternateAlleles(object.get(FeatureStringEnum.ALTERNATE_ALLELES.getValue()).isArray());
            }
            if (object.get(FeatureStringEnum.VARIANT_INFO.getValue()) != null) {
                annotationInfo.setVariantProperties(object.get(FeatureStringEnum.VARIANT_INFO.getValue()).isArray());
            }
        }

        if(object.containsKey(FeatureStringEnum.DBXREFS.getValue())){
            annotationInfo.setDbXrefList(DbXRefInfoConverter.convertToDbXrefFromArray(object.get(FeatureStringEnum.DBXREFS.getValue()).isArray()));
        }
        if(object.containsKey(FeatureStringEnum.ATTRIBUTES.getValue())){
            annotationInfo.setAttributeList(AttributeInfoConverter.convertToAttributeFromArray(object.get(FeatureStringEnum.ATTRIBUTES.getValue()).isArray()));
        }
        if(object.containsKey(FeatureStringEnum.COMMENTS.getValue())){
            annotationInfo.setCommentList(CommentInfoConverter.convertToCommentFromArray(object.get(FeatureStringEnum.COMMENTS.getValue()).isArray()));
        }

        annotationInfo.setMin((int) object.get(FeatureStringEnum.LOCATION.getValue()).isObject().get(FeatureStringEnum.FMIN.getValue()).isNumber().doubleValue());
        annotationInfo.setMax((int) object.get(FeatureStringEnum.LOCATION.getValue()).isObject().get(FeatureStringEnum.FMAX.getValue()).isNumber().doubleValue());

        if(object.get(FeatureStringEnum.LOCATION.getValue()).isObject().containsKey(FeatureStringEnum.IS_FMIN_PARTIAL.getValue())){
            annotationInfo.setPartialMin(object.get(FeatureStringEnum.LOCATION.getValue()).isObject().get(FeatureStringEnum.IS_FMIN_PARTIAL.getValue()).isBoolean().booleanValue());
        }
        if(object.get(FeatureStringEnum.LOCATION.getValue()).isObject().containsKey(FeatureStringEnum.IS_FMAX_PARTIAL.getValue())) {
            annotationInfo.setPartialMax(object.get(FeatureStringEnum.LOCATION.getValue()).isObject().get(FeatureStringEnum.IS_FMAX_PARTIAL.getValue()).isBoolean().booleanValue());
        }
        annotationInfo.setStrand((int) object.get(FeatureStringEnum.LOCATION.getValue()).isObject().get(FeatureStringEnum.STRAND.getValue()).isNumber().doubleValue());
        annotationInfo.setUniqueName(object.get(FeatureStringEnum.UNIQUENAME.getValue()).isString().stringValue());
        annotationInfo.setSequence(object.get(FeatureStringEnum.SEQUENCE.getValue()).isString().stringValue());
        if (object.get(FeatureStringEnum.OWNER.getValue()) != null) {
            annotationInfo.setOwner(object.get(FeatureStringEnum.OWNER.getValue()).isString().stringValue());
        }
        annotationInfo.setDateCreated(object.get(FeatureStringEnum.DATE_CREATION.getValue()).toString());
        annotationInfo.setDateLastModified(object.get(FeatureStringEnum.DATE_LAST_MODIFIED.getValue()).toString());
        List<String> noteList = new ArrayList<>();
        if (object.get(FeatureStringEnum.NOTES.getValue()) != null) {
            JSONArray jsonArray = object.get(FeatureStringEnum.NOTES.getValue()).isArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                String note = jsonArray.get(i).isString().stringValue();
                noteList.add(note);
            }
        }
        annotationInfo.setNoteList(noteList);


        if (processChildren && object.get(FeatureStringEnum.CHILDREN.getValue()) != null) {
            JSONArray jsonArray = object.get(FeatureStringEnum.CHILDREN.getValue()).isArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                AnnotationInfo childAnnotation = convertFromJsonObject(jsonArray.get(i).isObject(), true);
                annotationInfo.addChildAnnotation(childAnnotation);
            }
        }

        return annotationInfo;
    }


}
