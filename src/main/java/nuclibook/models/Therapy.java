package nuclibook.models;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import nuclibook.entity_utils.AbstractEntityUtils;
import nuclibook.server.Renderable;

import java.sql.SQLException;
import java.util.*;

/**
 * Model to represent a therapy.
 */
@DatabaseTable(tableName = "therapies")
public class Therapy implements Renderable {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(width = 64)
    private String name;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<BookingPatternSection> bookingPatternSections;

    @DatabaseField(columnName = "tracer_required", foreign = true, foreignAutoRefresh = true)
    private Tracer tracerRequired;

    @DatabaseField(width = 32, columnName = "tracer_dose")
    private String tracerDose;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<TherapyCameraType> therapyCameraTypes;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<PatientQuestion> patientQuestions;

    @DatabaseField(defaultValue = "true")
    private Boolean enabled;

	/**
	 * Blank constructor for ORM.
	 */
	public Therapy() {
	}

    /**
     * Gets the ID of the therapy.
     *
     * @return the ID of the therapy.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of the therapy.
     *
     * @param id the ID of the therapy.
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get the therapy's name.
     *
     * @return the therapy's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the therapy's name.
     *
     * @param name the therapy's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get a list the booking pattern sections of the therapy.
     *
     * @return a list of the booking pattern sections of the therapy.
     * @see nuclibook.models.BookingSection
     */
    public List<BookingPatternSection> getBookingPatternSections() {
        ArrayList<BookingPatternSection> output = new ArrayList<>();
        try {
            bookingPatternSections.refreshCollection();
        } catch (SQLException | NullPointerException e) {
            return output;
        }
        CloseableIterator<BookingPatternSection> iterator = bookingPatternSections.closeableIterator();
        try {
            BookingPatternSection bps;
            while (iterator.hasNext()) {
                bps = iterator.next();
                if (bps != null) output.add(bps);
            }

            // sort by sequence
            output.sort(new Comparator<BookingPatternSection>() {
                @Override
                public int compare(BookingPatternSection o1, BookingPatternSection o2) {
                    return o1.getSequence() - o2.getSequence();
                }
            });
        } finally {
            iterator.closeQuietly();
        }
        return output;
    }

    /**
     * Gets a list the booking pattern sections of the therapy in the form of a string.
     *
     * @return a list of the booking pattern sections of the therapy in the form of a string.
     * @see nuclibook.models.BookingPatternSection
     */
    public String getBookingPatternSectionListString() {
        List<BookingPatternSection> bookingPatternSectionList = getBookingPatternSections();
        if (bookingPatternSectionList.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (BookingPatternSection bps : bookingPatternSectionList) {
            sb.append("[").append(bps.isBusy() ? "1," : "0,").append(bps.getMinLength()).append(",").append(bps.getMaxLength()).append("],");
        }
        return sb.substring(0, sb.length() - 1) + "]";
    }

    /**
     * Gets the tracer required for the therapy.
     *
     * @return the tracer required for the therapy.
     * @see nuclibook.models.Tracer
     */
    public Tracer getTracerRequired() {
        return tracerRequired;
    }

    /**
     * Sets the tracer required for the therapy.
     *
     * @param tracerRequired the tracer required for the therapy.
     * @see nuclibook.models.Tracer
     */
    public void setTracerRequired(Tracer tracerRequired) {
        this.tracerRequired = tracerRequired;
    }

    /**
     * Gets the dose of the tracer required.
     *
     * @return the dose of the tracer required.
     * @see nuclibook.models.Tracer
     */
    public String getTracerDose() {
        return tracerDose;
    }

    /**
     * Sets the dose of the tracer required.
     *
     * @param tracerDose the dose of the tracer required.
     * @see nuclibook.models.Tracer
     */
    public void setTracerDose(String tracerDose) {
        this.tracerDose = tracerDose;
    }

    /**
     * Gets a list of suitable camera types.
     *
     * @return a list of suitable camera types.
     * @see nuclibook.models.CameraType
     */
    public List<CameraType> getCameraTypes() {
        ArrayList<CameraType> output = new ArrayList<>();
        CloseableIterator<TherapyCameraType> iterator = therapyCameraTypes.closeableIterator();
        try {
            CameraType ct;
            while (iterator.hasNext()) {
                ct = iterator.next().getCameraType();
                if (ct != null) output.add(ct);
            }
        } finally {
            iterator.closeQuietly();
        }
        return output;
    }

    /**
     * Gets a summarised String of suitable camera types.
     *
     * @return a summarised String of suitable camera types.
     * @see nuclibook.models.CameraType
     */
    public String getCameraTypesSummary() {
        // get camera types in a simple format
        List<CameraType> cameraTypes = getCameraTypes();

        // sort cameraTypes
        Collections.sort(cameraTypes, new Comparator<CameraType>() {
            @Override
            public int compare(CameraType o1, CameraType o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });

        if (cameraTypes.size() == 0) {
            return "None";
        }

        if (cameraTypes.size() == 1) {
            return cameraTypes.get(0).getLabel();
        }

        if (cameraTypes.size() == 2) {
            return cameraTypes.get(0).getLabel() + "<br />" + cameraTypes.get(1).getLabel();
        }

        String output = cameraTypes.get(0).getLabel() + "<br />" + cameraTypes.get(1).getLabel() + "<br />";
        output += "<div id=\"more-camera-types-" + getId() + "\" style=\"display: none;\">";
        for (int i = 2; i < cameraTypes.size(); ++i) {
            output += cameraTypes.get(i).getLabel() + "<br />";
        }
        output = output.substring(0, output.length() - 6);
        output += "</div>";
        output += "<span>+ " + (cameraTypes.size() - 2) + " more (<a href=\"javascript:;\" class=\"more-camera-types\" data-target=\"more-camera-types-" + getId() + "\">show</a>)</span>";
        return output;
    }

    /**
     * Gets a CSV string of the IDs of camera types, which are used in the front end.
     *
     * @return a CSV string of the IDs of camera types.
     * @see nuclibook.models.CameraType
     */
    public String getCameraTypesIdString() {
        List<CameraType> cameraTypeList = getCameraTypes();
        if (cameraTypeList.isEmpty()) return "0";
        StringBuilder sb = new StringBuilder();
        for (CameraType ct : cameraTypeList) {
            sb.append(ct.getId()).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * Removes all suitable camera types from this therapy.
     *
     * @see nuclibook.models.CameraType
     */
    public void clearCameraTypes() {
        if (therapyCameraTypes == null) return;
        CloseableIterator<TherapyCameraType> iterator = therapyCameraTypes.closeableIterator();
        try {
            while (iterator.hasNext()) {
                AbstractEntityUtils.deleteEntity(TherapyCameraType.class, iterator.next());
            }
        } finally {
            iterator.closeQuietly();
        }
    }

    /**
     * Adds a suitable camera type for this therapy.
     *
     * @param ct a camera type.
     * @see nuclibook.models.CameraType
     */
    public void addCameraType(CameraType ct) {
        AbstractEntityUtils.createEntity(TherapyCameraType.class, new TherapyCameraType(this, ct));
    }

    /**
     * Gets a list of questions that the user needs to ask the patient before booking the therapy.
     *
     * @return a list of questions that the user needs to ask the patient before booking the therapy.
     * @see nuclibook.models.PatientQuestion
     */
    public List<PatientQuestion> getPatientQuestions() {
        ArrayList<PatientQuestion> output = new ArrayList<>();
        try {
            patientQuestions.refreshCollection();
        } catch (SQLException | NullPointerException e) {
            return output;
        }
        CloseableIterator<PatientQuestion> iterator = patientQuestions.closeableIterator();
        try {
            PatientQuestion pq;
            while (iterator.hasNext()) {
                pq = iterator.next();
                if (pq != null) output.add(pq);
            }

            // sort by sequence
            output.sort(new Comparator<PatientQuestion>() {
                @Override
                public int compare(PatientQuestion o1, PatientQuestion o2) {
                    return o1.getSequence() - o2.getSequence();
                }
            });
        } finally {
            iterator.closeQuietly();
        }
        return output;
    }

    /**
     * Gets a list of questions that the user needs to ask the patient before booking
     * the therapy in the form of a String.
     *
     * @return a String with the list of questions a user asks the patient before booking a therapy
     * @see nuclibook.models.PatientQuestion
     */
    public String getPatientQuestionListString() {
        List<PatientQuestion> patientQuestionList = getPatientQuestions();
        if (patientQuestions.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (PatientQuestion pq : patientQuestionList) {
            sb.append("'").append(pq.getDescription().replace("'", "\\\\'")).append("',");
        }
        return sb.substring(0, sb.length() - 1) + "]";
    }

    /**
     * Gets whether it's enabled.
     *
     * @return true if it's enabled; false otherwise
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets whether it's enabled.
     *
     * @param enabled whether it's enabled.
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public HashMap<String, String> getHashMap() {
        return new HashMap<String, String>() {{
            put("id", getId().toString());
            put("name", getName());
            put("camera-type-ids", "IDLIST:" + getCameraTypesIdString());
            put("camera-type-summary", getCameraTypesSummary());
            put("CUSTOM:patient-questions", "CUSTOM:" + getPatientQuestionListString());
            put("CUSTOM:booking-pattern-sections", "CUSTOM:" + getBookingPatternSectionListString());
            put("tracer-required-id", getTracerRequired().getId().toString());
            put("tracer-required-name", getTracerRequired().getName());
            put("tracer-dose", getTracerDose());
            put("therapy-tracer-dose", getTracerDose());

            // advice note
            String advice = "This therapy requires " + getTracerRequired().getName() + " (" + getTracerRequired().getOrderTime() + " day order).//The recommended booking pattern is ";
            boolean adviceComma = false;
            List<BookingPatternSection> bookingPatternSections = getBookingPatternSections();
            if (bookingPatternSections.isEmpty()) {
                advice += "unknown.";
            } else {
                for (BookingPatternSection bps : bookingPatternSections) {
                    advice += adviceComma ? ", " : "";
                    adviceComma = true;
                    if (bps.getMinLength() == bps.getMaxLength()) {
                        advice += bps.getMinLength();
                    } else {
                        advice += bps.getMinLength() + "-" + bps.getMaxLength();
                    }
                    advice += " mins ";
                    advice += bps.isBusy() ? "booking" : "wait";
                }
                advice += ".";
            }

            put("advice", advice);
        }};
    }
}
