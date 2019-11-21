package org.proteininformationresource.pirsr;

/**
 * A class for PIRSR Info
 * 
 * @author Chuming Chen
 *
 */
public class PIRSR {
	
	private String ruleAC;
	private String trigger;
	private String srhmmAC;
	private String templateAC;
	private String templateSeq;
	/**
	 * @param ruleAC
	 * @param trigger
	 * @param srhmmAC
	 * @param templateAC
	 * @param templateSeq
	 */
	public PIRSR(String ruleAC, String trigger, String srhmmAC, String templateAC, String templateSeq) {
		super();
		this.ruleAC = ruleAC;
		this.trigger = trigger;
		this.srhmmAC = srhmmAC;
		this.templateAC = templateAC;
		this.templateSeq = templateSeq;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PIRSRInfo [ruleAC=" + ruleAC + ", trigger=" + trigger + ", srhmmAC=" + srhmmAC + ", templateAC=" + templateAC + ", templateSeq=" + templateSeq
				+ "]";
	}
	/**
	 * @return the ruleAC
	 */
	public String getRuleAC() {
		return ruleAC;
	}
	/**
	 * @param ruleAC the ruleAC to set
	 */
	public void setRuleAC(String ruleAC) {
		this.ruleAC = ruleAC;
	}
	/**
	 * @return the trigger
	 */
	public String getTrigger() {
		return trigger;
	}
	/**
	 * @param trigger the trigger to set
	 */
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	/**
	 * @return the srhmmAC
	 */
	public String getSrhmmAC() {
		return srhmmAC;
	}
	/**
	 * @param srhmmAC the srhmmAC to set
	 */
	public void setSrhmmAC(String srhmmAC) {
		this.srhmmAC = srhmmAC;
	}
	/**
	 * @return the templateAC
	 */
	public String getTemplateAC() {
		return templateAC;
	}
	/**
	 * @param templateAC the templateAC to set
	 */
	public void setTemplateAC(String templateAC) {
		this.templateAC = templateAC;
	}
	/**
	 * @return the templateSeq
	 */
	public String getTemplateSeq() {
		return templateSeq;
	}
	/**
	 * @param templateSeq the templateSeq to set
	 */
	public void setTemplateSeq(String templateSeq) {
		this.templateSeq = templateSeq;
	}
	
	
	
}
