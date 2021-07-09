package com.github.susom.starr.deid;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class FileHeaders {

	@SerializedName("FINDING_CNT_transcription")
	@Expose
	private Integer fINDINGCNTTranscription;
	@SerializedName("transcription")
	@Expose
	private String transcription;
	@SerializedName("FINDING_transcription")
	@Expose
	private String fINDINGTranscription;
	@SerializedName("TEXT_DEID_transcription")
	@Expose
	private String tEXTDEIDTranscription;
	@SerializedName("id")
	@Expose
	private String id;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public FileHeaders() {
	}

	/**
	 *
	 * @param fINDINGCNTTranscription
	 * @param transcription
	 * @param fINDINGTranscription
	 * @param tEXTDEIDTranscription
	 * @param id
	 */
	public FileHeaders(Integer fINDINGCNTTranscription, String transcription, String fINDINGTranscription, String tEXTDEIDTranscription, String id) {
		super();
		this.fINDINGCNTTranscription = fINDINGCNTTranscription;
		this.transcription = transcription;
		this.fINDINGTranscription = fINDINGTranscription;
		this.tEXTDEIDTranscription = tEXTDEIDTranscription;
		this.id = id;
	}

	public Integer getFINDINGCNTTranscription() {
		return fINDINGCNTTranscription;
	}

	public void setFINDINGCNTTranscription(Integer fINDINGCNTTranscription) {
		this.fINDINGCNTTranscription = fINDINGCNTTranscription;
	}

	public String getTranscription() {
		return transcription;
	}

	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	public String getFINDINGTranscription() {
		return fINDINGTranscription;
	}

	public void setFINDINGTranscription(String fINDINGTranscription) {
		this.fINDINGTranscription = fINDINGTranscription;
	}

	public String getTEXTDEIDTranscription() {
		return tEXTDEIDTranscription;
	}

	public void setTEXTDEIDTranscription(String tEXTDEIDTranscription) {
		this.tEXTDEIDTranscription = tEXTDEIDTranscription;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}